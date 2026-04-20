package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.service.AdminUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
public class AdminUploadController {
    private final AdminUploadService adminUploadService;

    @GetMapping
    public ResponseEntity<List<Episode>> loadUploadList() { return ResponseEntity.ok(adminUploadService.loadUploadList()); }

    @PostMapping("/episodes/{episodeId}/toggle")
    public ResponseEntity<String> handleToggleVisibility(@PathVariable Long episodeId) {
        adminUploadService.toggleVisibility(episodeId);
        return ResponseEntity.ok("상태 변경 완료");
    }

    @PostMapping("/episodes/{episodeId}/schedule")
    public ResponseEntity<String> handleScheduleSubmit(@PathVariable Long episodeId, @RequestBody Map<String, String> data) {
        LocalDateTime date = LocalDateTime.parse(data.get("scheduledAt"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        adminUploadService.handleScheduleSubmit(episodeId, date);
        return ResponseEntity.ok("예약 완료");
    }
}
