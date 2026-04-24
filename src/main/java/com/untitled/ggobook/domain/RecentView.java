package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_episode_id")
    private Episode lastEpisode;

    @Column(nullable = false)
    private int progress = 0;

    @Column(nullable = false)
    private LocalDateTime viewedAt;

    public RecentView(Long userId, Content content, Episode lastEpisode, int progress) {
        this.userId = userId;
        this.content = content;
        this.lastEpisode = lastEpisode;
        this.progress = progress;
        this.viewedAt = LocalDateTime.now();
    }

    public void updateViewStatus(Episode lastEpisode, int progress) {
        this.lastEpisode = lastEpisode;
        this.progress = progress;
        this.viewedAt = LocalDateTime.now();
    }
}