package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.*;
import com.untitled.ggobook.domain.enums.Status;
import com.untitled.ggobook.repository.*;
import com.untitled.ggobook.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EpisodeService {
    private final EpisodeRepository episodeRepository;
    private final ComicToonRepository comicToonRepository;
    private final NovelRepository novelRepository;
    private final FileUtil fileUtil;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final OwnedContentRepository ownedContentRepository;
    private final PointRepository pointRepository;
    private final ReadingRepository readingRepository;
    private final EpisodeLikeRepository episodeLikeRepository;

    @Transactional(readOnly = true)
    public Episode getEpisodeDetail(Long episodeId) {
        return episodeRepository.findByIdWithDetails(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차를 찾을 수 없습니다."));
    }

    @Transactional
    public Episode getEpisodeDetail(Long episodeId, Long id) {
        Episode episode = episodeRepository.findByIdWithDetails(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차를 찾을 수 없습니다."));

        // 🌟 핵심 수술: 옛날 데이터에 작가(Author)나 작품(Content) 정보가 비어있어도 서버가 터지지 않도록 방어막(Null 체크) 추가!
        boolean isAuthor = false;
        if (id != null && episode.getContent() != null && episode.getContent().getAuthor() != null) {
            isAuthor = episode.getContent().getAuthor().getId().equals(id);
        }

        // 작가가 아닌데 비공개(임시저장/반려) 상태면 차단
        if (!isAuthor && episode.getStatus() != Status.APPROVED && episode.getStatus() != Status.PUBLISHED) {
            throw new IllegalArgumentException("현재 비공개 처리된 회차입니다.");
        }

        // 미리보기 결제 체크 로직
        if (!isAuthor && episode.getStatus() == Status.APPROVED && !Boolean.TRUE.equals(episode.getIsFree())) {
            if (id == null) {
                throw new IllegalArgumentException("미리보기(유료) 회차는 로그인 후 구매해야 볼 수 있습니다.");
            }
            boolean isOwned = ownedContentRepository.existsByUserIdAndEpisode(id, episode);
            if (!isOwned) {
                throw new IllegalArgumentException("결제가 필요한 유료 회차입니다.");
            }
        }

        // 최근 읽은 위치 저장 (옛날 데이터에 content가 없으면 저장 패스)
        if (!isAuthor && id != null && episode.getContent() != null) {
            Reading reading = readingRepository
                    .findByUserIdAndContent(id, episode.getContent())
                    .orElse(new Reading());
            reading.setEpisode(episode);
            reading.setUserId(id);
            reading.setContent(episode.getContent());
            reading.setUpdatedAt(LocalDateTime.now());
            readingRepository.save(reading);
        }

        return episode;
    }

    public Slice<Episode> getEpisodeList(Long contentId, Pageable pageable) {
        return episodeRepository.findEpisodeListByContentId(contentId, pageable);
    }

    @Transactional
    public void registerEpisode(Content content, Episode episode, Novel novel, MultipartFile thumbFile, List<MultipartFile> episodeFiles) {
        if (thumbFile != null && !thumbFile.isEmpty()) {
            episode.setThumbnailUrl(fileUtil.uploadToS3(thumbFile));
        }

        episode.setContent(content);
        episode.setStatus(Status.PENDING);
        Episode savedEpisode = episodeRepository.save(episode);

        if(novel != null){
            novel.setEpisode(savedEpisode);
            novelRepository.save(novel);
        }else{
            if (episodeFiles != null && !episodeFiles.isEmpty()) {
                for (int com = 0; com < episodeFiles.size(); com++) {
                    ComicToon comicToon = new ComicToon();
                    comicToon.setEpisode(savedEpisode);
                    comicToon.setImageUrl(fileUtil.uploadToS3(episodeFiles.get(com)));
                    comicToon.setImageOrder(com + 1);
                    comicToonRepository.save(comicToon);
                }
            }
        }
    }

    @Transactional
    public void updateEpisode(Episode episode, Novel novel, MultipartFile thumbFile, List<MultipartFile> episodeFiles, List<String> imageOrder) {
        Episode existing = episodeRepository.findById(episode.getEpisodeId())
                .orElseThrow(() -> new IllegalArgumentException("회차 없음"));

        // 🌟 완벽한 객체지향 방식: 외부에서 new 하지 않고 안전하게 복제본(Draft) 생성
        Episode draft = Episode.createDraft(existing);

        draft.setEpisodeNumber(episode.getEpisodeNumber() != null ? episode.getEpisodeNumber() : existing.getEpisodeNumber());
        draft.setEpisodeTitle(episode.getEpisodeTitle() != null ? episode.getEpisodeTitle() : existing.getEpisodeTitle());
        draft.setIsFree(episode.getIsFree() != null ? episode.getIsFree() : existing.getIsFree());
        draft.setScheduledAt(episode.getScheduledAt() != null ? episode.getScheduledAt() : existing.getScheduledAt());

        if (thumbFile != null && !thumbFile.isEmpty()) {
            draft.setThumbnailUrl(fileUtil.uploadToS3(thumbFile));
        } else {
            draft.setThumbnailUrl(existing.getThumbnailUrl());
        }

        Episode savedDraft = episodeRepository.save(draft);

        // 🌟 수정됨: 원본 데이터(Novel, ComicToon)는 절대 삭제하지 않습니다! 복제본에 새로 맵핑합니다.
        if (novel != null) {
            // 🌟 억지로 ID를 지우지 않고, 원본 내용만 쏙 빼서 안전하게 새 복제본을 만듭니다.
            Novel draftNovel = Novel.createDraft(novel, savedDraft);
            novelRepository.save(draftNovel);
        }
        else if (imageOrder != null && !imageOrder.isEmpty()) {
            int newFileIndex = 0;
            for (int i = 0; i < imageOrder.size(); i++) {
                String currentOrderVal = imageOrder.get(i);

                ComicToon comicToon = new ComicToon();
                comicToon.setEpisode(savedDraft); // 복제본에 연결
                comicToon.setImageOrder(i + 1);

                if (currentOrderVal.equals("NEW_FILE")) {
                    comicToon.setImageUrl(fileUtil.uploadToS3(episodeFiles.get(newFileIndex++)));
                } else {
                    comicToon.setImageUrl(currentOrderVal); // 기존 이미지 URL 재사용
                }
                comicToonRepository.save(comicToon);
            }
        }
    }

    @Transactional
    public void deleteEpisode(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차가 없습니다."));

        if (episode.getThumbnailUrl() != null) {
            fileUtil.deleteFromS3(episode.getThumbnailUrl());
        }

        if (episode.getNovel() != null) {
            novelRepository.delete(episode.getNovel());
        }

        List<ComicToon> comicToons = comicToonRepository.findByEpisode(episode);
        for (ComicToon comicToon : comicToons) {
            fileUtil.deleteFromS3(comicToon.getImageUrl());
        }
        comicToonRepository.deleteAll(comicToons);

        episodeRepository.delete(episode);
    }

    @Transactional
    public void purchaseEpisode(Long id, Long episodeId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));
        Wallet wallet = walletRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Wallet이 존재하지 않습니다."));
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("회차가 존재하지 않습니다."));

        if (ownedContentRepository.existsByUserIdAndEpisode(id, episode)) {
            throw new RuntimeException("이미 구매한 회차입니다.");
        }

        int EPISODE_PRICE = 200;
        if (wallet.getBalance() < EPISODE_PRICE) {
            throw new RuntimeException("포인트가 부족합니다.");
        }

        wallet.setBalance(wallet.getBalance() - EPISODE_PRICE);
        walletRepository.save(wallet);

        Point point = new Point();
        point.setUser(user);
        point.setWallet(wallet);
        point.setPointType("DEDUCT");
        point.setAmount(EPISODE_PRICE);
        point.setDescription("작품 소장 - " + episode.getEpisodeTitle());
        pointRepository.save(point);

        OwnedContent ownedContent = new OwnedContent();
        ownedContent.setUserId(id);
        ownedContent.setContent(episode.getContent());
        ownedContent.setEpisode(episode);
        ownedContentRepository.save(ownedContent);
    }

    @Transactional
    public void toggleEpisodeLike(Long userId, Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("회차 찾을 수 없음"));

        Optional<EpisodeLike> existing = episodeLikeRepository.findByUserIdAndEpisode(userId, episode);

        if (existing.isEmpty()) {
            episodeLikeRepository.save(EpisodeLike.builder().userId(userId).episode(episode).build());
            episode.increaseLikeCount();
        } else {
            episodeLikeRepository.delete(existing.get());
            episode.decreaseLikeCount();
        }
    }

    @Transactional(readOnly = true)
    public boolean checkEpisodeLike(Long userId, Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("회차 없음"));
        return episodeLikeRepository.findByUserIdAndEpisode(userId, episode).isPresent();
    }

    @Transactional(readOnly = true)
    public Integer getNextEpisodeNumber(Long contentId) {
        return episodeRepository
                .findTopByContent_ContentIdOrderByEpisodeNumberDesc(contentId)
                .map(ep -> ep.getEpisodeNumber() + 1)
                .orElse(1);
    }

    @Transactional(readOnly = true)
    public Slice<Episode> getAuthorEpisodeList(Long contentId, Pageable pageable) {
        return episodeRepository.findAuthorEpisodeListByContentId(contentId, pageable);
    }
}