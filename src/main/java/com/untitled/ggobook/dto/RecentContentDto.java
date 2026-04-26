package com.untitled.ggobook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecentContentDto {
    private Long id;              // 작품 상세페이지 이동용 ID
    private String title;         // 작품명
    private String author;        // 작가명
    private String thumbnailUrl;  // 썸네일 이미지

    private Long lastEpisodeId;   // 이어보기 클릭 시 해당 뷰어로 꽂아줄 ID
    private String lastEpisode;   // "15화" (프론트 출력용)
    private int progress;         // 45 (%)
    private LocalDateTime date;   // 열람 시간

    // 🌟 핵심 추가: 프론트엔드 탭 필터링(WEBTOON/WEB_NOVEL)을 위한 이름표!
    private String contentType;
}