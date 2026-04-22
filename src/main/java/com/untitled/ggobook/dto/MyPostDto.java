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
    private String type;           // 웹툰, 웹소설 등
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private String status;         // DRAFT, PUBLISHED 등

    // Entity -> DTO 변환을 쉽게 해주는 정적 팩토리 메서드 (Clean Code)
    public static MyPostDto from(Content content) {
        return MyPostDto.builder()
                .contentId(content.getContentId())
                .title(content.getTitle())
                .type(content.getType())
                .thumbnailUrl(content.getThumbnailUrl())
                .createdAt(content.getCreatedAt())
                .status(content.getStatus().name())
                .build();
    }
}