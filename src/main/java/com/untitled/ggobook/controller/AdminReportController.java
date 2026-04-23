package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Report;
import com.untitled.ggobook.domain.enums.SuspensionDuration;
import com.untitled.ggobook.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    // 대기 중인 신고 목록 조회
    @GetMapping("/pending")
    public ResponseEntity<List<Report>> getPendingReports() {
        return ResponseEntity.ok(adminReportService.getPendingReports());
    }

    // 신고 승인 (유저 정지)
    @PostMapping("/{reportId}/approve")
    public ResponseEntity<String> approveReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request) {

        SuspensionDuration duration = SuspensionDuration.valueOf(request.get("duration"));
        String processReason = request.get("processReason");

        adminReportService.approveReportAndSuspendUser(reportId, duration, processReason);
        return ResponseEntity.ok("신고 처리 및 유저 정지가 완료되었습니다.");
    }

    // 단순 신고 완료 처리
    @PostMapping("/{reportId}/resolve")
    public ResponseEntity<String> resolveReportOnly(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request) {

        String processReason = request.get("processReason");
        adminReportService.resolveReportOnly(reportId, processReason);
        return ResponseEntity.ok("신고가 단순 완료 처리되었습니다.");
    }

    // 신고 기각 (허위 신고)
    @PostMapping("/{reportId}/reject")
    public ResponseEntity<String> rejectReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request) {

        String processReason = request.get("processReason");
        adminReportService.rejectReport(reportId, processReason);
        return ResponseEntity.ok("허위 신고로 기각 처리되었습니다.");
    }
}
