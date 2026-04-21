package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Notification;
import com.untitled.ggobook.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // ✅ 알림 생성 및 저장 (관리자 서비스에서 호출할 메서드)
    @Transactional
    public void send(Long receiverId, String message, Notification.NotificationType type, String url) {
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .message(message)
                .type(type)
                .relatedUrl(url)
                .build();
        notificationRepository.save(notification);
    }

    // 작가별 알림 목록 조회
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Long receiverId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    // 알림 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
        notification.setRead(true);
    }
}