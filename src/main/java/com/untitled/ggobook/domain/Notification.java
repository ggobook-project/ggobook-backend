package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private String message;

    public enum NotificationType {
        APPROVE,  // 승인 알림
        REJECT,   // 반려 알림
        INFO      // 기타 공지사항
    }

    // ✅ 위에서 만든 내부 Enum을 타입으로 사용합니다.
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String relatedUrl;
    private boolean isRead;
    // 🌟 추가: 삭제 여부를 상태로 관리하는 컬럼 (기본값은 false)
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    // 도메인 메서드: 상태를 '삭제됨'으로 변경
    public void markAsDeleted() {
        this.isDeleted = true;
    }
}