package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    // 신고한 회원 ID
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    // 신고 대상 ID (content_id, episode_id, comment_id, relay_entry_id 등)
    @Column(nullable = false)
    private Long targetId;

    // 신고 대상 타입 (CONTENT / EPISODE / COMMENT / RELAY)
    @Column(nullable = false, length = 20)
    private String targetType;

    // 신고 사유 타입
    @Column(nullable = false, length = 500)
    private String reasonType;

    // 처리 상태 (PENDING / PROCESSED / DELETED)
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    // 처리 사유 (관리자 입력)
    @Column(length = 500)
    private String processReason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
