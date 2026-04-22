package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private Long authorId; // 작성 관리자 ID

    private Long viewCount = 0L;

    // 🌟 [추가] 중요 공지 상단 고정 여부
    private boolean isPinned = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder
    public Notice(String title, String content, Long authorId, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.isPinned = isPinned;
    }

    // 🌟 [비즈니스 메서드] 수정 로직
    public void update(String title, String content, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.isPinned = isPinned;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}