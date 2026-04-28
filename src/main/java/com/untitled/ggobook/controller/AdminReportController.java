package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Report;
import com.untitled.ggobook.domain.enums.SuspensionDuration;
import com.untitled.ggobook.service.AdminReportService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    // 🌟 요청 데이터를 담을 내부 DTO (유지보수 향상)
    @Data
    public static class ReportActionRequest {
        private String duration;      // 정지 기간 (승인시에만 사용)
        private String processReason; // 처리 사유
    }

    // 대기 중인 신고 목록 조회
    @GetMapping("/pending")
    public ResponseEntity<List<Report>> getPendingReports() {
        return ResponseEntity.ok(adminReportService.getPendingReports());
    }

    // 1. 신고 승인 (유저 정지 + 중복 신고 일괄 처리)
    @PostMapping("/{reportId}/approve")
    public ResponseEntity<String> approveReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal Long adminId,
            @RequestBody ReportActionRequest request) {

        // 문자열로 들어온 duration을 Enum으로 변환
        SuspensionDuration duration = SuspensionDuration.valueOf(request.getDuration());

        adminReportService.approveReportAndSuspendUser(
                reportId, adminId, duration, request.getProcessReason());

        return ResponseEntity.ok("신고 승인 및 해당 게시물 관련 신고들이 일괄 처리되었습니다.");
    }

    // 2. 단순 신고 완료 처리 (중복 신고 일괄 처리)
    @PostMapping("/{reportId}/resolve")
    public ResponseEntity<String> resolveReportOnly(
            @PathVariable Long reportId,
            @AuthenticationPrincipal Long adminId,
            @RequestBody ReportActionRequest request) {

        adminReportService.resolveReportOnly(reportId, adminId, request.getProcessReason());

        return ResponseEntity.ok("신고 및 관련 신고들이 단순 완료 처리되었습니다.");
    }

    // 3. 신고 기각 (해당 건만 기각)
    @PostMapping("/{reportId}/reject")
    public ResponseEntity<String> rejectReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal Long adminId,
            @RequestBody ReportActionRequest request) {

        adminReportService.rejectReport(reportId, adminId, request.getProcessReason());

        return ResponseEntity.ok("해당 신고가 기각 처리되었습니다.");
    }
}