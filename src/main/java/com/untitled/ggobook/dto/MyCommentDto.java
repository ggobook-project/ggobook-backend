package com.untitled.ggobook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyCommentDto {
    private Long id;              // 댓글 ID (포커싱용)
    private String content;       // 댓글 내용
    private String contentTitle;  // 작품명
    private Long contentId;       // 작품 ID (라우팅용)
    private Long episodeId;       // 회차 ID (라우팅용)
    private String episode;       // "15화"
    private String date;          // 작성일 (예: "2026.04.13")
    private String contentType;   // 🌟 탭 필터링 및 라우팅용 ("WEBTOON" / "WEB_NOVEL")
}