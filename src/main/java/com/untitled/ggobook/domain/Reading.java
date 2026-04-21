package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@Table(
        name = "reading",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_content_reading",
                        columnNames = {"user_id", "content_id"}
                )
        }
)
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}