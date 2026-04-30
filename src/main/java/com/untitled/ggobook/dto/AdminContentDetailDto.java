package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Content;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminContentDetailDto {
    private Long contentId;
    private String title;
    private String type;
    private String genre;
    private String summary;
    private String description;
    private String thumbnailUrl;
    private String status;
    private LocalDateTime createdAt;
    private AuthorInfo author;

    @Getter
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String nickname;
        private String email;
    }

    public static AdminContentDetailDto from(Content content) {
        AuthorInfo authorInfo = content.getAuthor() == null ? null :
                AuthorInfo.builder()
                        .id(content.getAuthor().getId())
                        .nickname(content.getAuthor().getNickname())
                        .email(content.getAuthor().getEmail())
                        .build();

        return AdminContentDetailDto.builder()
                .contentId(content.getContentId())
                .title(content.getTitle())
                .type(content.getType())
                .genre(content.getGenre())
                .summary(content.getSummary())
                .description(content.getDescription())
                .thumbnailUrl(content.getThumbnailUrl())
                .status(content.getStatus().name())
                .createdAt(content.getCreatedAt())
                .author(authorInfo)
                .build();
    }
}
