package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.dto.AdminContentDetailDto;
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

    // 1. 검수 대기 목록 조회 (PENDING 작품 목록)
    @GetMapping
    public ResponseEntity<List<AdminContentDetailDto>> getInspectionList() {
        return ResponseEntity.ok(
            adminService.getInspectionList().stream()
                .map(AdminContentDetailDto::from)
                .collect(java.util.stream.Collectors.toList())
        );
    }

    // 작품 상세 조회
    @GetMapping("/contents/{contentId}")
    public ResponseEntity<AdminContentDetailDto> getContentDetail(@PathVariable Long contentId) {
        return ResponseEntity.ok(AdminContentDetailDto.from(adminService.getContentDetail(contentId)));
    }

    // 작품 승인
    @PostMapping("/contents/{contentId}/approve")
    public ResponseEntity<String> approveContent(@PathVariable Long contentId) {
        adminService.approveContent(contentId);
        return ResponseEntity.ok("작품이 승인되었습니다.");
    }

    // 작품 반려
    @PostMapping("/contents/{contentId}/reject")
    public ResponseEntity<String> rejectContent(
            @PathVariable Long contentId,
            @RequestBody Map<String, String> requestData) {
        adminService.rejectContent(contentId, requestData.get("rejectReason"));
        return ResponseEntity.ok("작품이 반려되었습니다.");
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
        adminService.approveEpisode(episodeId, scheduledAt);
        return ResponseEntity.ok("성공적으로 승인되었습니다.");
    }

    // 4. 회차 반려 처리 (작품 연쇄 반려 포함)
    @PostMapping("/episodes/{episodeId}/reject")
    public ResponseEntity<String> rejectEpisode(
            @PathVariable Long episodeId,
            @RequestBody Map<String, String> requestData) {

        Episode episode = episodeService.getEpisodeDetail(episodeId);
        String rejectReason = requestData.get("rejectReason");
        adminService.rejectEpisode(episodeId, rejectReason);
        return ResponseEntity.ok("반려 처리가 완료되었습니다.");
    }

    // ==========================================
    //  5. [추가] 일반 작품 회차 블라인드 API
    // ==========================================
    @PostMapping("/episodes/{episodeId}/blind")
    public ResponseEntity<String> blindContent(
            @PathVariable Long episodeId,
            @RequestBody Map<String, String> requestData) {

        String reason = requestData.get("reason");
        adminService.blindContent(episodeId, reason);
        return ResponseEntity.ok("작품 회차가 강제 블라인드 처리되었습니다.");
    }
}