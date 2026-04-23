package com.untitled.ggobook.domain;

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

    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    private Long viewCount = 0L;
    private Integer likeCount = 0;
    private Double rating = 0.0;
    private String rejectReason;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ToString.Exclude
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Episode> episodes = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    private List<ContentTag> tags = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    private List<Rating> ratings = new ArrayList<>();

    public void approve() {
        this.status = Status.APPROVED;
    }

    public void reject(String reason) {
        this.status = Status.REJECTED;
        this.rejectReason = reason;
    }
}