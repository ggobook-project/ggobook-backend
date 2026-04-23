package com.untitled.ggobook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "relay_topic")
public class RelayTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long topicId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 주제를 만든 유저 ID

    // 핵심: 관리자 주제를 참조(FK). null 허용으로 '자유 주제' 기능 지원
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_topic_id")
    @JsonIgnore
    private AdminRelayTopic adminTopic;

    @Column(nullable = false, length = 100)
    private String title; // 유저가 정한 구체적 제목

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 이 주제 아래에서 연재되는 소설 목록 (1:N)
    @ToString.Exclude
    @OneToMany(mappedBy = "relayTopic", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<RelayNovel> relayNovels = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}