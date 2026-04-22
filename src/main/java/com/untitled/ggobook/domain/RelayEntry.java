package com.untitled.ggobook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.untitled.ggobook.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "relay_entry")
public class RelayEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long entryId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relay_novel_id", nullable = false)
    @JsonIgnore
    private RelayNovel relayNovel;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String entryText;

    // 🌟 [추가] 관리자 메시지 (AI 요약본 저장용)
    @Column(columnDefinition = "TEXT")
    private String adminMessage;

    // 🌟 [추가] 회차 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PUBLISHED;

    @Column(nullable = false)
    private Integer entryOrder;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 🌟 [추가] 릴레이 블라인드 처리 로직 (상태 변경 및 요약본 삽입)
    public void blind(String safeSummary) {
        this.status = Status.BLINDED;
        this.adminMessage = safeSummary;
    }
}