package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Reply;
import com.untitled.ggobook.domain.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ReplyResponseDto {
    private Long replyId;
    private Long userId;
    private String replyText;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private Integer likeCount;
    private Integer dislikeCount;
    private String myReaction;


    private String nickname;

    private String profileImageUrl;

    public static ReplyResponseDto from(Reply reply) {
        return ReplyResponseDto.builder()
                .replyId(reply.getReplyId())
                .userId(reply.getUser().getId()) // user 객체에서 꺼냄
                .nickname(reply.getUser().getNickname() != null ? reply.getUser().getNickname() : "알 수 없음") // 닉네임 꺼냄!
                .profileImageUrl(reply.getUser().getStatus() == UserStatus.WITHDRAWN ? null : reply.getUser().getProfileImageUrl())
                .replyText(reply.getIsDeleted() ? "삭제된 답글입니다." : reply.getReplyText())
                .createdAt(reply.getCreatedAt())
                .isDeleted(reply.getIsDeleted())
                .likeCount(reply.getLikeCount() != null ? reply.getLikeCount() : 0)
                .dislikeCount(reply.getDislikeCount() != null ? reply.getDislikeCount() : 0)
                .build();
    }
}