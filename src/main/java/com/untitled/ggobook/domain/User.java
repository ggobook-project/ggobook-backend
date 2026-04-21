package com.untitled.ggobook.domain;

import com.untitled.ggobook.domain.enums.UserStatus;
import com.untitled.ggobook.domain.enums.SuspensionDuration;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String userId;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 10)
    private String gender;

    @Column(length = 20)
    private String role; // ROLE_USER, ROLE_ADMIN 등

    // ==========================================
    // 신고 및 정지 관리 필드
    // ==========================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE; // 기본값은 활동 상태

    private LocalDateTime suspensionEndDate; // 정지 해제 예정 시간

    // ==========================================
    // 정지 및 해제 비즈니스 로직 (도메인 메서드)
    // ==========================================

    /**
     * 유저 정지 처리
     */
    public void suspend(SuspensionDuration duration) {
        this.status = UserStatus.SUSPENDED;

        if (duration == SuspensionDuration.PERMANENT) {
            // 영구 정지 시 9999년으로 설정
            this.suspensionEndDate = LocalDateTime.of(9999, 12, 31, 23, 59);
        } else {
            // 현재 시간으로부터 지정된 일수만큼 더함
            this.suspensionEndDate = LocalDateTime.now().plusDays(duration.getDays());
        }
    }

    /**
     * 유저 활동 상태로 복구
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.suspensionEndDate = null;
    }

    // ==========================================
    // 소셜 로그인 정보 갱신용
    // ==========================================
    public User update(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }
}