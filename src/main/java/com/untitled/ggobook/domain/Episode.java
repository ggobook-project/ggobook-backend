package com.untitled.ggobook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String rejectReason;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ToString.Exclude
    @OneToOne(mappedBy = "episode", cascade = CascadeType.ALL)
    private Novel novel;

    @ToString.Exclude
    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private List<ComicToon> comicToons = new ArrayList<>();

    // 🌟 상태 변경 도메인 로직 (Service에서 호출)
    public void blind(String reason) {
        this.status = Status.BLINDED;
        this.rejectReason = reason; // 관리자 메모용으로 재활용
    }

    public String getExtractableTextForAI() {
        if (this.content == null || this.content.getType() == null) {
            return null;
        }

        String type = this.content.getType();

        if (("NOVEL".equalsIgnoreCase(type) || "웹소설".equals(type)) && this.novel != null) {
            return this.novel.getContentText();
        }
        return null;
    }

    public void approve(LocalDateTime scheduledAt, String aiSummary) {
        this.status = Status.APPROVED;
        this.scheduledAt = scheduledAt;

        // AI 요약이 유효한 경우에만 저장하고, 실패했거나 없으면 null로 유지
        if (aiSummary != null && !aiSummary.startsWith("AI 요약 생성에 실패")) {
            this.aiSummary = aiSummary;
        }
    }

    public void reject(String reason) {
        this.status = Status.REJECTED;
        this.rejectReason = reason;
    }
}