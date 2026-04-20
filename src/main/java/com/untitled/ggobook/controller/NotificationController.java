package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Notification;
import com.untitled.ggobook.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // 로그인한 작가의 알림 목록 가져오기
    @GetMapping("/{authorId}")
    public ResponseEntity<List<Notification>> getMyNotifications(@PathVariable Long authorId) {
        return ResponseEntity.ok(notificationService.getNotifications(authorId));
    }

    // 알림 읽음 표시 (클릭 시 호출)
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
}