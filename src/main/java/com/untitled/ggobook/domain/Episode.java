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
}