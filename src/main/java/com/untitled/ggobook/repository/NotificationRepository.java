package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 🌟 수정: 삭제되지 않은 알림만 가져오도록 통일
    List<Notification> findByReceiverIdAndIsDeletedFalseOrderByCreatedAtDesc(Long receiverId);

    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.receiver.id = :receiverId")
    void softDeleteAllByReceiverId(@Param("receiverId") Long receiverId);

    long countByReceiverIdAndIsReadFalse(Long receiverId);
}