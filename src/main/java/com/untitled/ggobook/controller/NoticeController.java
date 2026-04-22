package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Notice;
import com.untitled.ggobook.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<Page<Notice>> getNoticeList(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(noticeService.getNoticeList(pageable));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<Notice> getNoticeDetail(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNoticeDetail(noticeId));
    }
}