package com.untitled.ggobook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.untitled.ggobook.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 🌟 안전한 객체 생성
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    private String type;

    private String title;
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnailUrl;
    private String genre;
    private String serialDay;

    private String videoUrl;

    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    private Long viewCount = 0L;
    private Integer likeCount = 0;
    private Double rating = 0.0;
    private String rejectReason;
    private LocalDateTime createdAt = LocalDateTime.now();

    // 🌟 주간 랭킹 점수 (최근 7일 데이터 기반으로 매일 새벽 업데이트됨)
    private Double weeklyScore = 0.0;

    @ToString.Exclude
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Episode> episodes = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ContentTag> tags = new ArrayList<>();


    public void approve() {
        this.status = Status.APPROVED;
    }

    public void reject(String reason) {
        this.status = Status.REJECTED;
        this.rejectReason = reason;
    }
}