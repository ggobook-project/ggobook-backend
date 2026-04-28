package com.untitled.ggobook.domain;

import com.untitled.ggobook.domain.enums.ReactionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplyReaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id", nullable = false)
    private Reply reply;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType reactionType;

    @Builder
    public ReplyReaction(Long userId, Reply reply, ReactionType reactionType) {
        this.userId = userId;
        this.reply = reply;
        this.reactionType = reactionType;
    }

    public void changeReaction(ReactionType type) {
        this.reactionType = type;
    }
}