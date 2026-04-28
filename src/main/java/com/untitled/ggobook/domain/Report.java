package com.untitled.ggobook.domain;

import com.untitled.ggobook.domain.enums.ReportReason;
import com.untitled.ggobook.domain.enums.ReportStatus;
import com.untitled.ggobook.domain.enums.TargetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
// 🌟 무분별한 객체 생성을 막기 위해 기본 생성자는 Protected로 닫아둡니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    // ==========================================
    // 🌟 수정 1: 연관관계 매핑 (Reporter & Reported User)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter; // 신고한 회원 (객체로 연결)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser; // 🚨 신고당한 회원 (정지 대상을 바로 뽑아오기 위해 추가!)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    // ==========================================
    // 기존의 훌륭한 설계 유지: 무엇을 신고했는가?
    // ==========================================
    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TargetType targetType;

    // ==========================================
    // 🌟 수정 2: Enum 적용으로 타입 안정성 확보
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportReason reportReason; // 신고 사유 (스팸, 욕설 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING; // 기본값: PENDING

    @Column(length = 500)
    private String processReason; // 관리자 처리 사유

    // ==========================================
    // 🌟 수정 3: 시간 데이터 보호 (updatable = false)
    // ==========================================
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();


    // ==========================================
    // 🌟 수정 4: 상태 변경 비즈니스 메서드 (도메인 주도 설계)
    // ==========================================

    @Builder
    public Report(User reporter, User reportedUser, Long targetId, TargetType targetType, ReportReason reportReason) {
        this.reporter = reporter;
        this.reportedUser = reportedUser;
        this.targetId = targetId;
        this.targetType = targetType;
        this.reportReason = reportReason;
    }

    public void rejectReport(User admin, String processReason) {
        this.admin = admin;
        this.status = ReportStatus.REJECTED;
        this.processReason = processReason;
        this.updatedAt = LocalDateTime.now();
    }

    public void resolveReport(User admin, String processReason) {
        this.admin = admin;
        this.status = ReportStatus.RESOLVED;
        this.processReason = processReason;
        this.updatedAt = LocalDateTime.now();
    }
}