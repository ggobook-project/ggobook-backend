package com.untitled.ggobook.controller;

import com.untitled.ggobook.service.AdminNoticeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    @PostMapping
    public ResponseEntity<Void> registerNotice(@RequestBody NoticeRequest request) {
        Long adminId = 1L; // 추후 시큐리티 적용
        adminNoticeService.registerNotice(request.getTitle(), request.getContent(), request.isPinned(), adminId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{noticeId}")
    public ResponseEntity<Void> updateNotice(@PathVariable Long noticeId, @RequestBody NoticeRequest request) {
        adminNoticeService.updateNotice(noticeId, request.getTitle(), request.getContent(), request.isPinned());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long noticeId) {
        adminNoticeService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class NoticeRequest {
        private String title;
        private String content;
        private boolean isPinned;
    }
}