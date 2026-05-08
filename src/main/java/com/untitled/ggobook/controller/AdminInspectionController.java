package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.dto.AdminEpisodeDetailDto;
import com.untitled.ggobook.service.AdminInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/inspections")
@RequiredArgsConstructor
public class AdminInspectionController {

    private final AdminInspectionService adminService;

    @GetMapping("/approved")
    public ResponseEntity<List<Episode>> getApprovedList() {
        return ResponseEntity.ok(adminService.getApprovedList());
    }

    // 🌟 수술 1: 작품(Content)과 회차(Episode)를 묶어서 통합 검수 목록으로 반환!
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getInspectionList() {
        return ResponseEntity.ok(adminService.getInspectionMergedList());
    }

    // ==========================================
    //  [신설] 작품(Content) 전용 검수 API
    // ==========================================
    @GetMapping("/contents/{contentId}")
    public ResponseEntity<Map<String, Object>> getContentDetail(@PathVariable Long contentId) {
        return ResponseEntity.ok(adminService.getContentInspectionDetail(contentId));
    }

    @PostMapping("/contents/{contentId}/approve")
    public ResponseEntity<String> approveContent(@PathVariable Long contentId) {
        adminService.approveContent(contentId);
        return ResponseEntity.ok("작품이 성공적으로 승인되었습니다.");
    }

    @PostMapping("/contents/{contentId}/reject")
    public ResponseEntity<String> rejectContent(
            @PathVariable Long contentId,
            @RequestBody Map<String, String> requestData) {
        adminService.rejectContent(contentId, requestData.get("rejectReason"));
        return ResponseEntity.ok("작품이 반려되었습니다.");
    }

    // ==========================================
    //  [기존 유지] 회차(Episode) 전용 검수 API
    // ==========================================
    @GetMapping("/episodes/{episodeId}")
    public ResponseEntity<AdminEpisodeDetailDto> getEpisodeDetail(@PathVariable Long episodeId) {
        Episode episode = adminService.getEpisodeDetail(episodeId);
        return ResponseEntity.ok(AdminEpisodeDetailDto.from(episode));
    }

    @PostMapping("/episodes/{episodeId}/approve")
    public ResponseEntity<String> approveEpisode(
            @PathVariable Long episodeId,
            @RequestBody Map<String, String> requestData) {

        LocalDateTime scheduledAt = LocalDateTime.parse(
                requestData.get("scheduledAt"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
        adminService.approveEpisode(episodeId, scheduledAt);
        return ResponseEntity.ok("회차가 성공적으로 승인되었습니다.");
    }

    @PostMapping("/episodes/{episodeId}/reject")
    public ResponseEntity<String> rejectEpisode(
            @PathVariable Long episodeId,
            @RequestBody Map<String, String> requestData) {

        adminService.rejectEpisode(episodeId, requestData.get("rejectReason"));
        return ResponseEntity.ok("회차 반려 처리가 완료되었습니다.");
    }

    @PostMapping("/episodes/{episodeId}/blind")
    public ResponseEntity<String> blindContent(
            @PathVariable Long episodeId,
            @RequestBody Map<String, String> requestData) {

        adminService.blindContent(episodeId, requestData.get("reason"));
        return ResponseEntity.ok("작품 회차가 강제 블라인드 처리되었습니다.");
    }
}