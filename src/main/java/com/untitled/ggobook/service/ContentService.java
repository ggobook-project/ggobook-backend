package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.ContentTag;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.enums.Status;
import com.untitled.ggobook.dto.ContentDetailDto;
import com.untitled.ggobook.dto.EpisodeDetailDto;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.repository.*;
import com.untitled.ggobook.util.FileUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final EpisodeRepository episodeRepository;
    private final UserRepository userRepository;
    private final FileUtil fileUtil;
    private final LikeRepository likeRepository;
    private final ReadingRepository readingRepository;
    private final OwnedContentRepository ownedContentRepository;
    private final ContentTagRepository contentTagRepository;

    @Transactional
    public Slice<Content> getContentList(String keyword, String genre, String type, String sortType,String serialDay, Pageable pageable) {
        String searchKeyword = (keyword == null || keyword.isBlank()) ? null : keyword;
        String searchGenre = (genre == null || genre.isBlank()) ? null : genre;
        String searchDay = (serialDay == null || serialDay.isBlank()) ? null : serialDay;

        if ("popular".equals(sortType)) {
            return contentRepository.findPopularContentList(searchKeyword, searchGenre, type, searchDay, pageable);
        } else {
            return contentRepository.findContentList(searchKeyword, searchGenre, type,searchDay, pageable);
        }
    }

    @Transactional
    public ContentDetailDto getContentDetail(Long contentId,Long userId, Pageable pageable, String currentNeedStatus) {

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품 없음"));

        content.setViewCount(content.getViewCount() + 1);

        Slice<Episode> episodes = episodeRepository.findEpisodeListByContentId(contentId, pageable);

        boolean isLiked = false;
        if (userId != null) {
            isLiked = likeRepository.findByUserIdAndContent_ContentId(userId, contentId) != null;
        }

        Slice<EpisodeDetailDto> episodeDtos = episodes.map(ep -> {
            EpisodeDetailDto dto = new EpisodeDetailDto();
            dto.setEpisodeId(ep.getEpisodeId());
            dto.setEpisodeNumber(ep.getEpisodeNumber());
            dto.setEpisodeTitle(ep.getEpisodeTitle());

            // ==========================================
            // 🌟 미리보기 vs 영구유료 완벽 분기 처리
            // ==========================================
            LocalDateTime now = LocalDateTime.now();
            boolean isTimePassed = ep.getScheduledAt() == null || !ep.getScheduledAt().isAfter(now);

            if (Boolean.TRUE.equals(ep.getIsFree())) {
                // 1. 작가가 '무료'로 올렸을 때
                if (!isTimePassed) {
                    dto.setIsFree(false);
                    dto.setStatus("PREVIEW"); // 🌟 시간이 안 지났으면 프론트에 '미리보기'라고 명시해 줌
                } else {
                    dto.setIsFree(true);
                    dto.setStatus("PUBLISHED"); // 시간이 지나면 완전 무료로 전환
                }
            } else {
                // 2. 작가가 처음부터 '유료'로 올렸을 때
                dto.setIsFree(false);
                dto.setStatus(ep.getStatus().name()); // APPROVED든 PUBLISHED든 원래 상태 유지 (영구 유료)
            }
            // ==========================================

            dto.setCreatedAt(ep.getScheduledAt() != null ? ep.getScheduledAt() : ep.getCreatedAt());
            dto.setThumbnailUrl(ep.getThumbnailUrl());
            dto.setIsRead(userId != null && readingRepository.existsByUserIdAndEpisode_EpisodeId(userId, ep.getEpisodeId()));

            boolean isOwned = false;
            if (userId != null) isOwned = ownedContentRepository.existsByUserIdAndEpisode(userId, ep);
            dto.setIsOwned(isOwned);
            return dto;
        });

        List<ContentTag> tags = contentTagRepository.findByContent_ContentId(contentId);

        return new ContentDetailDto(
                content.getContentId(),
                content.getTitle(),
                content.getType(),
                content.getGenre(),
                content.getSummary(),
                content.getThumbnailUrl(),
                episodeDtos,
                isLiked,
                tags,
                content.getDescription(),
                content.getSerialDay(),
                content.getVideoUrl(),
                content.getAuthor() != null ? content.getAuthor().getNickname() : "미상"
        );
    }

    @Transactional
    public Content registerContent(Content content, MultipartFile multipartFile, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        content.setAuthor(author);
        if (!multipartFile.isEmpty()) {
            content.setThumbnailUrl(fileUtil.uploadToS3(multipartFile));
        }
        content.setStatus(Status.PENDING);
        return contentRepository.save(content);
    }

    @Transactional
    public void updateContent(Content content, MultipartFile multipartFile){
        Content existing = contentRepository.findById(content.getContentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 작품이 없습니다."));

        // 🌟 완벽한 객체지향 방식: 외부에서 new 하지 않고 엔티티에 복제본 생성을 요청합니다.
        Content draft = Content.createDraft(existing);

        // 변경된 필드만 덮어쓰거나 기존 값 유지
        draft.setTitle(content.getTitle() != null ? content.getTitle() : existing.getTitle());
        draft.setType(content.getType() != null ? content.getType() : existing.getType());
        draft.setGenre(content.getGenre() != null ? content.getGenre() : existing.getGenre());
        draft.setSummary(content.getSummary() != null ? content.getSummary() : existing.getSummary());
        draft.setDescription(content.getDescription() != null ? content.getDescription() : existing.getDescription());
        draft.setSerialDay(content.getSerialDay() != null ? content.getSerialDay() : existing.getSerialDay());
        draft.setVideoUrl(content.getVideoUrl() != null ? content.getVideoUrl() : existing.getVideoUrl());

        if (multipartFile != null && !multipartFile.isEmpty()) {
            draft.setThumbnailUrl(fileUtil.uploadToS3(multipartFile));
        } else {
            draft.setThumbnailUrl(existing.getThumbnailUrl()); // 사진 안 바꿨으면 원본 사진 유지
        }
        contentRepository.save(draft);
    }

    @Transactional
    public void deleteContent(Long contentId){
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작품이 없습니다."));

        if (content.getThumbnailUrl() != null) {
            fileUtil.deleteFromS3(content.getThumbnailUrl());
        }
        contentRepository.deleteById(contentId);
    }

    @Transactional
    public Content getContentByContentID(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품 없음"));
    }

    @Transactional
    public Slice<Content> getMyContents(Long authorId, Pageable pageable) {
        return contentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    }
}