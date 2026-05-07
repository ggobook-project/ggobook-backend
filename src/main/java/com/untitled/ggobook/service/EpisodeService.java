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

    // 1. 관리자/비로그인/내부 시스템용 (프리패스)
    @Transactional(readOnly = true)
    public Episode getEpisodeDetail(Long episodeId) {
        return episodeRepository.findByIdWithDetails(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차를 찾을 수 없습니다."));
    }

    // 🌟 2. 일반 독자용 (미리보기 유료 결제 검문소 완벽 적용)
    @Transactional
    public Episode getEpisodeDetail(Long episodeId, Long id) {

        Episode episode = episodeRepository.findByIdWithDetails(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차를 찾을 수 없습니다."));

        // [방어 1] 비공개, 검수대기, 반려, 블라인드 회차 접근 완벽 차단
        if (episode.getStatus() != Status.APPROVED && episode.getStatus() != Status.PUBLISHED) {
            throw new IllegalArgumentException("현재 비공개 처리된 회차입니다.");
        }

        // [방어 2] 🌟 APPROVED(유료 미리보기) 상태일 때 결제(소장) 여부 확인!
        // (단, 작가가 애초에 1화처럼 '무료'로 설정해둔 회차면 결제 없이 통과)
        if (episode.getStatus() == Status.APPROVED && !Boolean.TRUE.equals(episode.getIsFree())) {
            if (id == null) {
                throw new IllegalArgumentException("미리보기(유료) 회차는 로그인 후 구매해야 볼 수 있습니다.");
            }
            boolean isOwned = ownedContentRepository.existsByUserIdAndEpisode(id, episode);
            if (!isOwned) {
                throw new IllegalArgumentException("결제가 필요한 유료 회차입니다."); // 프론트에서 이 에러를 잡으면 결제창 띄우기
            }
        }

        // 정상 접근이면 읽은 기록(Reading) 저장
        if(id != null) {
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
    public void updateEpisode(Episode episode, Novel novel, MultipartFile thumbFile, List<MultipartFile> episodeFiles) {
        Episode existing = episodeRepository.findById(episode.getEpisodeId())
                .orElseThrow(() -> new IllegalArgumentException("회차 없음"));

        if (thumbFile != null && !thumbFile.isEmpty()) {
            fileUtil.deleteFromS3(existing.getThumbnailUrl());
            episode.setThumbnailUrl(fileUtil.uploadToS3(thumbFile));
        }

        Episode savedEpisode = episodeRepository.save(episode);

        if (novel != null) {
            novel.setEpisodeId(savedEpisode.getEpisodeId());
            novel.setEpisode(savedEpisode);
            novelRepository.save(novel);
        } else if (episodeFiles != null && !episodeFiles.isEmpty()) {
            List<ComicToon> oldComicToons = comicToonRepository.findByEpisode(savedEpisode);
            for (ComicToon old : oldComicToons) {
                fileUtil.deleteFromS3(old.getImageUrl());
            }
            comicToonRepository.deleteAll(oldComicToons);

            for (int i = 0; i < episodeFiles.size(); i++) {
                ComicToon comicToon = new ComicToon();
                comicToon.setEpisode(savedEpisode);
                comicToon.setImageUrl(fileUtil.uploadToS3(episodeFiles.get(i)));
                comicToon.setImageOrder(i + 1);
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
}