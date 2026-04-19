package com.untitled.ggobook.domain;

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

    // 등록한 관리자 ID
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 이 주제를 사용한 릴레이 소설 목록 (양방향)
    @ToString.Exclude
    @OneToMany(mappedBy = "relayTopic", cascade = CascadeType.ALL)
    private List<RelayNovel> relayNovels = new ArrayList<>();
}