package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@Table(
        name = "owned_content",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_episode_owned",
                        columnNames = {"user_id", "episode_id"}
                )
        }
)
public class OwnedContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ownedId;

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
    private LocalDateTime createdAt = LocalDateTime.now();
}