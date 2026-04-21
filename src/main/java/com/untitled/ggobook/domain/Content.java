package com.untitled.ggobook.domain;

import com.untitled.ggobook.domain.enums.Status;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 작품 도메인
@Entity
@Data
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    private Long authorId;
    private String type;
    private String title;
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnailUrl;
    private String genre;
    private String serialDay;

    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    private Long viewCount = 0L;
    private Integer likeCount = 0;
    private Double rating = 0.0;

    private String rejectReason;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 양방향 관계 설정 및 무한 루프 방지
    @ToString.Exclude
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Episode> episodes = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    private List<ContentTag> tags = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    private List<Rating> ratings = new ArrayList<>();
}