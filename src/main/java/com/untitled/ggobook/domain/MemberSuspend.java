package com.untitled.ggobook.domain;

import com.untitled.ggobook.domain.enums.SuspensionDuration;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_suspend")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSuspend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long suspendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 정지 대상 회원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin; // 처리한 관리자

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SuspensionDuration duration;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public MemberSuspend(User user, User admin, String reason, SuspensionDuration duration, LocalDateTime endDate) {
        this.user = user;
        this.admin = admin;
        this.reason = reason;
        this.duration = duration;
        this.startDate = LocalDateTime.now();
        this.endDate = endDate;
    }
}