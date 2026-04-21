package com.untitled.ggobook.domain.enums;

// 1. 신고 및 정지 사유 (커뮤니티/웹소설 표준)
public enum ReportReason {
    SPAM("스팸 및 도배"),
    ABUSIVE_LANGUAGE("욕설 및 비하 발언"),
    INAPPROPRIATE_CONTENT("음란물 및 선정적 콘텐츠"),
    COPYRIGHT_INFRINGEMENT("저작권 침해 및 무단 도용"),
    ILLEGAL_PROMOTION("불법 홍보 및 광고"),
    OTHER("기타 사유");

    private final String description;
    ReportReason(String description) { this.description = description; }
    public String getDescription() { return description; }
}
