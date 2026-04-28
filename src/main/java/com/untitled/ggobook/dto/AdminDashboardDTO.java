package com.untitled.ggobook.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardDTO {
    // 상단 Stats용
    private Long pendingInspectionCount; // 검수 대기
    private Long reportCount;            // 신고 접수
    private Long totalUserCount;         // 전체 회원(User)
    private Long todayJoinCount;         // 오늘 가입자
    private Integer inspectionBadge;
    private Integer reportBadge;
}
