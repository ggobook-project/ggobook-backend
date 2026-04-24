package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Notification;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.repository.NotificationRepository;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ✅ 알림 생성 및 저장 (관리자 서비스에서 호출할 메서드)
    @Transactional
    public void send(Long receiverId, String message, Notification.NotificationType type, String url) {
        // 1. 넘겨받은 ID로 User 엔티티를 먼저 조회합니다.
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 2. 이제 빌더에 receiver(객체)를 전달합니다.
        Notification notification = Notification.builder()
                .receiver(receiver) // 🌟 여기를 receiverId(Long) 대신 receiver(User 객체)로 수정!
                .message(message)
                .type(type)
                .relatedUrl(url)
                .build();

        notificationRepository.save(notification);
    }

    // 작가별 알림 목록 조회
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Long receiverId) {
        // 🌟 수정: 바뀐 메서드명으로 호출
        return notificationRepository.findByReceiverIdAndIsDeletedFalseOrderByCreatedAtDesc(receiverId);
    }

    // 알림 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
        notification.setRead(true);
    }

    // 🌟 수정: 개별 알림 소프트 삭제
    // NotificationService.java 내부

    @Transactional
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림이 존재하지 않습니다."));

        // 🌟 물리적으로 delete 하는 대신 상태만 변경 (Dirty Checking으로 자동 반영)
        notification.markAsDeleted();
    }

    @Transactional
    public void deleteAllNotifications(Long receiverId) {
        // 🌟 리포지토리에 만든 벌크 업데이트 쿼리 호출
        notificationRepository.softDeleteAllByReceiverId(receiverId);
    }
}