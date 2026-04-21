package com.untitled.ggobook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.untitled.ggobook.domain.enums.Status;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Episode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long episodeId;

    @JsonIgnore
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

    // 양방향 설정
    @ToString.Exclude
    @OneToOne(mappedBy = "episode", cascade = CascadeType.ALL)
    private Novel novel;

    @ToString.Exclude
    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private List<ComicToon> comicToons = new ArrayList<>();

    // AI 추출 메서드 (캡슐화 완벽 적용)
    public String getExtractableTextForAI() {
        if (this.content == null || this.content.getType() == null) {
            return null;
        }

        String type = this.content.getType();

        if ("NOVEL".equalsIgnoreCase(type) && this.novel != null) {
            return this.novel.getContentText();
        }
        // ✅ 웹툰("COMIC")이거나 그 외의 경우 요약할 텍스트가 없다고 명시적으로 null을 반환합니다.
        return null;
    }
}