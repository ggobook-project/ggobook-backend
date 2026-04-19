package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "relay_novel")
public class RelayNovel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long relayNovelId;

    // 최초 등록 회원 ID
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 관리자 주제 (없으면 NULL)
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private RelayTopic relayTopic;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 이어쓰기 목록 (양방향)
    @ToString.Exclude
    @OneToMany(mappedBy = "relayNovel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelayEntry> entries = new ArrayList<>();
}