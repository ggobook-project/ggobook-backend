package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Content;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikedContentDto {
    private Long contentId;
    private String title;
    private String author;         // 작가명
    private String type;
    private String status;         // 완결/연재중
    private String thumbnailUrl;

    // 🌟 팩트: 파라미터에 'String authorName'을 추가해서 외부에서 이름을 꽂아주도록 변경!
    public static LikedContentDto from(Content content, String authorName) {
        return LikedContentDto.builder()
                .contentId(content.getContentId())
                .title(content.getTitle())
                .author(authorName) // 🌟 찾아온 진짜 이름 주입
                .type(content.getType())
                .status(content.getStatus().name()) // 🌟 Enum -> String 변환 (핵심!)
                .thumbnailUrl(content.getThumbnailUrl())
                .build();
    }
}