package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "member_suspend")
public class MemberSuspend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long suspendId;

    // 정지 대상 회원 ID
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 처리한 관리자 ID
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}