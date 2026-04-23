package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CommentResponseDto {
    private Long commentId;
    private Long userId;
    private String commentText;
    private Boolean isSpoiler;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private Boolean isDeleted;

    // 🌟 부모 안에 자식 답글 리스트를 품습니다 (트리 구조)
    private List<ReplyResponseDto> replies;

    public static CommentResponseDto from(Comment comment) {
        // 자식 답글들도 싹 다 예쁜 DTO로 변환
        List<ReplyResponseDto> replyDtos = comment.getReplies().stream()
                .map(ReplyResponseDto::from)
                .collect(Collectors.toList());

        return CommentResponseDto.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                // 🌟 소프트 삭제 마법: 삭제된 댓글이면 내용을 블라인드 처리
                .commentText(comment.getIsDeleted() ? "삭제된 댓글입니다." : comment.getCommentText())
                .isSpoiler(comment.getIsSpoiler())
                .likeCount(comment.getLikeCount())
                .createdAt(comment.getCreatedAt())
                .isDeleted(comment.getIsDeleted())
                .replies(replyDtos)
                .build();
    }
}