package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@Table(
        name = "recent_view",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_content_recent_view",
                        columnNames = {"user_id", "content_id"}
                )
        }
)
public class RecentView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recentViewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_episode_id")
    private Episode lastEpisode;

    @Column(nullable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();
}