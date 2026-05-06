package com.untitled.ggobook.domain;

import com.untitled.ggobook.domain.enums.UserStatus;
import com.untitled.ggobook.domain.enums.SuspensionDuration;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate // 🌟 저장 시 자동으로 현재 시간 삽입
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // ==========================================
    // 신고 및 정지 관리 필드
    // ==========================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE; // 기본값은 활동 상태

    private LocalDateTime suspensionEndDate; // 정지 해제 예정 시간


    @Column(length = 500)
    private String profileImageUrl;

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
     * 유저 정지 해제
     */
    public void release() {
        this.status = UserStatus.ACTIVE; // 활성 상태로 복구 (본인의 Enum 상태명에 맞게 수정)
        this.suspensionEndDate = null;   // 정지 종료일 초기화
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

    // ==========================================
    // 🌟 추가: 유저 탈퇴 처리 (Soft Delete)
    // ==========================================
    /**
     * [회원 탈퇴] Soft Delete 및 개인정보 익명화
     * 즉시 재가입이 가능하도록 유니크 컬럼(userId, email, nickname)을 더미값으로 변경합니다.
     * PK(id)는 유지되어 기존에 작성한 게시물/댓글과의 연결이 깨지지 않습니다.
     */
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;

        // 고유 번호(id)를 활용한 절대 겹치지 않는 더미 접두사 생성
        String dummyPrefix = "withdrawn_" + this.id + "_";

        // 1. 유니크(Unique) 제약 조건 해제 (기존 정보 해방)
        this.userId = dummyPrefix + this.userId;
        this.email = dummyPrefix + this.email;
        this.nickname = "탈퇴한회원_" + this.id; // 닉네임도 유니크 컬럼이므로 더미화 필요

        // 2. 기타 개인정보 익명화 및 파기
        this.name = "알수없음";
        this.password = "WITHDRAWN_ACCOUNT_PROTECTED";
        this.suspensionEndDate = null;
    }
}