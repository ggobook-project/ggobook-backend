package com.untitled.ggobook.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.untitled.ggobook.domain.Notice;
import com.untitled.ggobook.service.AdminNoticeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    @GetMapping
    public ResponseEntity<Page<Notice>> getNotices(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminNoticeService.getNotices(pageable));
    }

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
        @JsonProperty("isPinned")
        private boolean isPinned;
    }
}