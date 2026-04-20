package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 특정 작가의 알림 목록을 최신순으로 조회
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    // 읽지 않은 알림 개수 확인 (종 아이콘 옆 숫자 표시용)
    long countByReceiverIdAndIsReadFalse(Long receiverId);
}