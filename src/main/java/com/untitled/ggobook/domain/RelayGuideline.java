package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class RelayGuideline {
    @Id
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 가이드라인 상세 내용

    private LocalDateTime updatedAt;

    @PreUpdate
    @PrePersist
    public void updateTime() {
        this.updatedAt = LocalDateTime.now();
    }
}