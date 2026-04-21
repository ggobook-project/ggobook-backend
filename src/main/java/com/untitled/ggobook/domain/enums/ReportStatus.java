package com.untitled.ggobook.domain.enums;

// 3. 신고 처리 상태
public enum ReportStatus {
    PENDING("대기중"),
    RESOLVED("처리완료(정지)"),
    REJECTED("허위신고(기각)");

    private final String description;
    ReportStatus(String description) { this.description = description; }
}
