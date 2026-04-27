package com.untitled.ggobook.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.untitled.ggobook.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Episode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long episodeId;

    @JsonIgnoreProperties("episodes")
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    private Integer episodeNumber;
    private String episodeTitle;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    private String thumbnailUrl;
    private Boolean isFree = true;

    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    private Long viewCount = 0L;

    // 🌟 추가: 회차 좋아요 캐싱 컬럼
    @Column(nullable = false)
    private Long likeCount = 0L;

    private String rejectReason;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ToString.Exclude
    @OneToOne(mappedBy = "episode", cascade = CascadeType.ALL)
    private Novel novel;

    @ToString.Exclude
    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private List<ComicToon> comicToons = new ArrayList<>();

    public void increaseLikeCount() { this.likeCount++; }
    public void decreaseLikeCount() { this.likeCount--; }

    public void blind(String reason) {
        this.status = Status.BLINDED;
        this.rejectReason = reason;
    }

    public String getExtractableTextForAI() {
        if (this.content == null || this.content.getType() == null) return null;
        String type = this.content.getType();
        if (("NOVEL".equalsIgnoreCase(type) || "웹소설".equals(type)) && this.novel != null) {
            return this.novel.getContentText();
        }
        return null;
    }

    public void approve(LocalDateTime scheduledAt, String aiSummary) {
        this.status = Status.APPROVED;
        this.scheduledAt = scheduledAt;
        if (aiSummary != null && !aiSummary.startsWith("AI 요약 생성에 실패")) {
            this.aiSummary = aiSummary;
        }
    }

    public void reject(String reason) {
        this.status = Status.REJECTED;
        this.rejectReason = reason;
    }
}