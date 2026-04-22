package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Comment;
import com.untitled.ggobook.domain.Reply;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyActivityDto {
    private Long id;              // 댓글ID 또는 답글ID
    private String activityType;  // "COMMENT" 또는 "REPLY"
    private String contentTitle;  // 작품명
    private String episodeTitle;  // 회차명
    private String text;          // 내가 쓴 내용
    private LocalDateTime createdAt;

    // 부모 댓글(Comment)을 받았을 때 변환
    public static MyActivityDto fromComment(Comment comment) {
        return MyActivityDto.builder()
                .id(comment.getCommentId())
                .activityType("COMMENT")
                .contentTitle(comment.getContent().getTitle())
                // 🌟 수정 완료: getTitle() -> getEpisodeTitle()
                .episodeTitle(comment.getEpisode().getEpisodeTitle())
                .text(comment.getCommentText())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    // 자식 답글(Reply)을 받았을 때 변환
    public static MyActivityDto fromReply(Reply reply) {
        return MyActivityDto.builder()
                .id(reply.getReplyId())
                .activityType("REPLY")
                .contentTitle(reply.getComment().getContent().getTitle())
                // 🌟 수정 완료: getTitle() -> getEpisodeTitle()
                .episodeTitle(reply.getComment().getEpisode().getEpisodeTitle())
                .text(reply.getReplyText())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}