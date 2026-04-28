package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Reply;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter; // 🌟 추가

import java.time.LocalDateTime;

@Getter
@Setter // 🌟 추가
@Builder
public class ReplyResponseDto {
    private Long replyId;
    private Long userId;
    private String replyText;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private Integer likeCount;
    private Integer dislikeCount; // 🌟 추가
    private String myReaction;    // 🌟 추가

    public static ReplyResponseDto from(Reply reply) {
        return ReplyResponseDto.builder()
                .replyId(reply.getReplyId())
                .userId(reply.getUserId())
                .replyText(reply.getIsDeleted() ? "삭제된 답글입니다." : reply.getReplyText())
                .createdAt(reply.getCreatedAt())
                .isDeleted(reply.getIsDeleted())
                .likeCount(reply.getLikeCount() != null ? reply.getLikeCount() : 0)
                .dislikeCount(reply.getDislikeCount() != null ? reply.getDislikeCount() : 0) // null 방지
                .build();
    }
}