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

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class  ContentService {

    private final ContentRepository contentRepository;
    private final EpisodeRepository episodeRepository;
    private final UserRepository userRepository;
    private final FileUtil fileUtil;
    private final LikeRepository likeRepository;
    private final ReadingRepository readingRepository;
    private final OwnedContentRepository ownedContentRepository;
    private final ContentTagRepository contentTagRepository;

    @Transactional
    public Slice<Content> getContentList(String keyword, String genre, String type, String sortType, Pageable pageable) {
        String searchKeyword = (keyword == null || keyword.isBlank()) ? null : keyword;
        String searchGenre = (genre == null || genre.isBlank()) ? null : genre;

        // 🌟 핵심 수술: 프론트에서 "popular"라고 외치면 랭킹순 쿼리로 보내고, 아니면 기존 최신순 쿼리로 보냅니다!
        if ("popular".equals(sortType)) {
            return contentRepository.findPopularContentList(searchKeyword, searchGenre, type, pageable);
        } else {
            return contentRepository.findContentList(searchKeyword, searchGenre, type, pageable);
        }
    }

    @Transactional
    public ContentDetailDto getContentDetail(Long contentId,Long userId, Pageable pageable, String currentNeedStatus) {

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품 없음"));

        content.setViewCount(content.getViewCount() + 1);


        Slice<Episode> episodes = episodeRepository.findEpisodeListByContentId(contentId, pageable, currentNeedStatus);

        boolean isLiked = false;
        if (userId != null) {
            isLiked = likeRepository.findByUserIdAndContent_ContentId(userId, contentId) != null;
        }

        Slice<EpisodeDetailDto> episodeDtos = episodes.map(ep -> {
            EpisodeDetailDto dto = new EpisodeDetailDto();
            dto.setEpisodeId(ep.getEpisodeId());
            dto.setEpisodeNumber(ep.getEpisodeNumber());
            dto.setEpisodeTitle(ep.getEpisodeTitle());
            dto.setIsFree(ep.getIsFree());
            dto.setStatus(ep.getStatus().name());
            dto.setCreatedAt(ep.getCreatedAt());
            dto.setThumbnailUrl(ep.getThumbnailUrl());
            dto.setIsRead(readingRepository.existsByUserIdAndEpisode_EpisodeId(userId, ep.getEpisodeId()));
            dto.setIsOwned(false);
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
                content.getThumbnailUrl()


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

        content.setAuthor(existing.getAuthor());

        if (multipartFile != null && !multipartFile.isEmpty()) {
            fileUtil.deleteFromS3(existing.getThumbnailUrl());
            content.setThumbnailUrl(fileUtil.uploadToS3(multipartFile));
        } else {
            content.setThumbnailUrl(existing.getThumbnailUrl());
        }
        contentRepository.save(content);
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
