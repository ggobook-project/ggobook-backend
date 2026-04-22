package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Content;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikedContentDto {
    private Long contentId;
    private String title;
    private String type;           // 웹툰, 소설 구분
    private String thumbnailUrl;
    private Double rating;         // 별점 표시용

    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static LikedContentDto from(Content content) {
        return LikedContentDto.builder()
                .contentId(content.getContentId())
                .title(content.getTitle())
                .type(content.getType())
                .thumbnailUrl(content.getThumbnailUrl())
                .rating(content.getRating())
                .build();
    }
}