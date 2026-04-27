package com.untitled.ggobook.dto;

import lombok.Data;

@Data
public class RelayNovelCreateRequestDTO {
    // 1. 주제 관련 정보
    private Long adminTopicId;  // 관리자 주제 선택 시 사용
    private String customTitle; // 자유 주제 시 사용
    private String customDescription; // 자유 주제 시 사용

    // 2. 소설 자체 정보
    private String novelTitle;

    // 3. 1회차 작성 정보
    private String firstEntryText;
}
