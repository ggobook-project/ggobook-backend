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
@Table(name = "admin_relay_topic")
public class AdminRelayTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminTopicId;

    @Column(nullable = false, length = 100)
    private String title; // 예: "신년맞이 판타지 특집"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 이 공식 주제를 기반으로 생성된 유저들의 주제들 (1:N)
    @ToString.Exclude
    @OneToMany(mappedBy = "adminTopic")
    @JsonIgnore
    private List<RelayTopic> userTopics = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}