package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.service.AdminInspectionService;
import com.untitled.ggobook.service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/admin/inspections")
@RequiredArgsConstructor
public class AdminInspectionController {

    private final AdminInspectionService adminService;
    private final EpisodeService episodeService;

    @GetMapping("/approved")
    public ResponseEntity<List<Episode>> getApprovedList() {
        return ResponseEntity.ok(adminService.getApprovedList());
    }

    // 1. 통합 검수 대기 목록 조회 (모든 신규 작품/회차 목록)
    @GetMapping
    public ResponseEntity<List<Episode>> getInspectionList() {
        System.out.println(adminService.getApprovedList());
        return ResponseEntity.ok(adminService.getInspectionList());
    }

    // 2. 특정 회차 상세 내용 조회 (관리자가 클릭 시 원고 읽기용)
    @GetMapping("/episodes/{episodeId}")
    public ResponseEntity<Episode> getEpisodeDetail(@PathVariable Long episodeId) {
        return ResponseEntity.ok(episodeService.getEpisodeDetail(episodeId));
    }

    // 3. 회차 승인 처리 (AI 요약 + 작품 승인 연동)
    @PostMapping("/episodes/{episodeId}/approve")
    public ResponseEntity<String> approveEpisode(
            @PathVariable Long episodeId,
            @RequestBody Map<String, String> requestData) {

        LocalDateTime scheduledAt = LocalDateTime.parse(
                requestData.get("scheduledAt"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
        Episode episode = episodeService.getEpisodeDetail(episodeId);
        adminService.approveEpisode(episode, scheduledAt);
        return ResponseEntity.ok("성공적으로 승인되었습니다.");
    }

    // 4. 회차 반려 처리 (작품 연쇄 반려 포함)
    @PostMapping("/episodes/{episodeId}/reject")
    public ResponseEntity<String> rejectEpisode(
            @PathVariable Long episodeId,
            @RequestBody Map<String, String> requestData) {

        Episode episode = episodeService.getEpisodeDetail(episodeId);
        String rejectReason = requestData.get("rejectReason");
        adminService.rejectEpisode(episode, rejectReason);
        return ResponseEntity.ok("반려 처리가 완료되었습니다.");
    }
}