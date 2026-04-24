package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Notification;
import com.untitled.ggobook.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public ResponseEntity<List<Notification>> getMyNotifications(@AuthenticationPrincipal Long authorId) {
        // 이미 꺼내진 authorId를 바로 서비스로 넘깁니다.
        return ResponseEntity.ok(notificationService.getNotifications(authorId));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAllMyNotifications(@AuthenticationPrincipal Long authorId) {
        notificationService.deleteAllNotifications(authorId);
        return ResponseEntity.ok().build();
    }

    // 알림 읽음 표시 로직 유지
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    // 🌟 알림 삭제 API (DELETE 방식 사용)
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        // 서비스 계층의 삭제 로직 호출 (DB에서 해당 ID의 알림 삭제)
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok().build();
    }
}