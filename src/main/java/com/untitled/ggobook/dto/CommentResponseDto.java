package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Comment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter; // 🌟 추가

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter // 🌟 서비스에서 내 반응만 살짝 덧입히기 위해 추가
@Builder
public class CommentResponseDto {
    private Long commentId;
    private Long userId;
    private String commentText;
    private Boolean isSpoiler;
    private Integer likeCount;
    private Integer dislikeCount; // 🌟 싫어요 개수 전광판
    private String myReaction;    // 🌟 내 반응 상태 ("LIKE", "DISLIKE", null)
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private List<ReplyResponseDto> replies;

    // 🌟닉네임 필드를 추가
    private String nickname;

    public static CommentResponseDto from(Comment comment) {
        List<ReplyResponseDto> replyDtos = comment.getReplies().stream()
                .map(ReplyResponseDto::from)
                .collect(Collectors.toList());

        return CommentResponseDto.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname() != null ? comment.getUser().getNickname() : "알 수 없음")
                .commentText(comment.getIsDeleted() ? "삭제된 댓글입니다." : comment.getCommentText())
                .isSpoiler(comment.getIsSpoiler())
                .likeCount(comment.getLikeCount())
                .dislikeCount(comment.getDislikeCount() != null ? comment.getDislikeCount() : 0)
                .createdAt(comment.getCreatedAt())
                .isDeleted(comment.getIsDeleted())
                .replies(replyDtos) // 완벽하게 포장된 답글 리스트
                .build();
    }
}