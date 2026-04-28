package com.untitled.ggobook.domain;

import com.untitled.ggobook.domain.enums.ReactionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentReaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType reactionType;

    @Builder
    public CommentReaction(Long userId, Comment comment, ReactionType reactionType) {
        this.userId = userId;
        this.comment = comment;
        this.reactionType = reactionType;
    }

    public void changeReaction(ReactionType type) {
        this.reactionType = type;
    }
}