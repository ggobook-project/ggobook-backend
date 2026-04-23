package com.untitled.ggobook.domain.enums;

// 1. 신고 및 정지 사유 (커뮤니티/웹소설 표준)
public enum ReportReason {
    SPAM("스팸 홍보/도배글 게시"),
    ABUSIVE_LANGUAGE("욕설 및 혐오 발언"),
    INAPPROPRIATE_CONTENT("음란물/선정적 콘텐츠 게시"),
    COPYRIGHT_INFRINGEMENT("타인 저작권 침해 및 무단 도용"),
    ILLEGAL_PROMOTION("불법 사이트 홍보 및 광고"),
    POLITICAL_DISPUTE("정치적 분쟁 유도 및 조장"),
    FAKE_INFORMATION("허위 사실 유포"),
    OTHER("기타 사유");

    private final String description;
    ReportReason(String description) { this.description = description; }
    public String getDescription() { return description; }
}
