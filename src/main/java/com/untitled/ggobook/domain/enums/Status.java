package com.untitled.ggobook.domain.enums;

public enum Status {
    DRAFT,      // 임시 저장
    PENDING,    // 검수 대기
    APPROVED,   // 검수 완료 (예약 상태)
    PUBLISHED,  // 공개 완료
    REJECTED, // 반려
    BLINDED     // 관리자/시스템에 의한 강제 블라인드
}