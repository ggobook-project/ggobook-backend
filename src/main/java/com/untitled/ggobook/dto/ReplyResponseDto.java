package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Reply;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReplyResponseDto {
    private Long replyId;
    private Long userId; // 프론트에서 본인 답글인지 체크(삭제 버튼 노출)할 때 씁니다.
    private String replyText;
    private LocalDateTime createdAt;
    private Boolean isDeleted;

    public static ReplyResponseDto from(Reply reply) {
        return ReplyResponseDto.builder()
                .replyId(reply.getReplyId())
                .userId(reply.getUserId())
                // 🌟 소프트 삭제 마법: 삭제된 답글이면 텍스트를 가립니다!
                .replyText(reply.getIsDeleted() ? "삭제된 답글입니다." : reply.getReplyText())
                .createdAt(reply.getCreatedAt())
                .isDeleted(reply.getIsDeleted())
                .build();
    }
}