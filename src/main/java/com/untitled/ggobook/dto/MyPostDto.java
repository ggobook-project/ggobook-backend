package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Content;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 작성 글 요약본
@Getter
@Builder
public class MyPostDto {
    private Long contentId;
    private String title;
    private String type;
    private String genre;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private String status;

    public static MyPostDto from(Content content) {
        return MyPostDto.builder()
                .contentId(content.getContentId())
                .title(content.getTitle())
                .type(content.getType())
                .genre(content.getGenre())
                .thumbnailUrl(content.getThumbnailUrl())
                .createdAt(content.getCreatedAt())
                .status(content.getStatus().name())
                .build();
    }
}