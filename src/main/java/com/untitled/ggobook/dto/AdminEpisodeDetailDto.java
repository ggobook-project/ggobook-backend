package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.ComicToon; // 🌟 ComicToon import 추가
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class AdminEpisodeDetailDto {
    // --- 1. 회차(Episode) 정보 ---
    private Long episodeId;
    private String episodeTitle;
    private Integer episodeNumber;
    private String episodeStatus;
    private String rejectReason;
    private LocalDateTime createdAt;

    // --- 관리자 원고 검수용 데이터 ---
    private String episodeText; // 웹소설 원고
    private List<String> imageUrls; // 웹툰 이미지 (순서대로 정렬됨)

    // --- 2. 작품(Content) 기본 정보 ---
    private Long contentId;
    private String title;
    private String type;
    private String genre;
    private String summary;
    private String description;
    private String thumbnailUrl;
    private String contentStatus;

    // --- 3. 작가(Author) 정보 ---
    private AuthorInfo author;

    @Getter
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String nickname;
        private String email;
    }

    public static AdminEpisodeDetailDto from(Episode episode) {
        Content content = episode.getContent();

        // 1. 작가 정보 안전하게 추출
        AuthorInfo authorInfo = (content != null && content.getAuthor() != null) ?
                AuthorInfo.builder()
                        .id(content.getAuthor().getId())
                        .nickname(content.getAuthor().getNickname())
                        .email(content.getAuthor().getEmail())
                        .build() : null;

        // 🌟 2. 핵심 로직: ComicToon 도메인 분석 결과 반영 (imageOrder 기준 오름차순 정렬)
        List<String> imageUrls = new ArrayList<>();
        if (episode.getComicToons() != null && !episode.getComicToons().isEmpty()) {
            imageUrls = episode.getComicToons().stream()
                    .sorted(Comparator.comparing(ComicToon::getImageOrder)) // 순서대로 정렬!
                    .map(ComicToon::getImageUrl)
                    .collect(Collectors.toList());
        }

        // 3. 최종 DTO 조립
        return AdminEpisodeDetailDto.builder()
                .episodeId(episode.getEpisodeId())
                .episodeTitle(episode.getEpisodeTitle())
                .episodeNumber(episode.getEpisodeNumber())
                .episodeStatus(episode.getStatus() != null ? episode.getStatus().name() : null)
                .rejectReason(episode.getRejectReason())
                .createdAt(episode.getCreatedAt())

                .episodeText(episode.getExtractableTextForAI()) // 팀원분 메서드 활용
                .imageUrls(imageUrls) // 정렬된 이미지 리스트

                .contentId(content != null ? content.getContentId() : null)
                .title(content != null ? content.getTitle() : "제목 없음")
                .type(content != null ? content.getType() : "미상")
                .genre(content != null ? content.getGenre() : "미상")
                .summary(content != null ? content.getSummary() : "")
                .description(content != null ? content.getDescription() : "")
                .thumbnailUrl(content != null ? content.getThumbnailUrl() : null)
                .contentStatus(content != null && content.getStatus() != null ? content.getStatus().name() : null)

                .author(authorInfo)
                .build();
    }
}