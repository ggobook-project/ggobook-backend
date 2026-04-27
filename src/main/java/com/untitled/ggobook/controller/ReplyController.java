package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.ReactionRequestDto;
import com.untitled.ggobook.dto.ReplyRequestDto;
import com.untitled.ggobook.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<String> createReply(
            @AuthenticationPrincipal Long id,
            @PathVariable Long commentId,
            @RequestBody ReplyRequestDto requestDto) {
        replyService.createReply(id, commentId, requestDto);
        return ResponseEntity.ok("답글 등록 완료");
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<String> deleteReply(
            @AuthenticationPrincipal Long id,
            @PathVariable Long replyId) {
        replyService.deleteReply(id, replyId);
        return ResponseEntity.ok("답글 삭제 완료");
    }

    // 🌟 1. 답글 수정 API (새로 추가)
    // 리액트에서 api.put(`/api/replies/${replyId}`) 로 접근할 때 여기로 들어옵니다.
    @PutMapping("/replies/{replyId}")
    public ResponseEntity<String> updateReply(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long replyId,
            @RequestBody ReplyRequestDto requestDto) {

        replyService.updateReply(userId, replyId, requestDto);
        return ResponseEntity.ok("답글이 성공적으로 수정되었습니다.");
    }

    // 🌟 2. 답글 좋아요/싫어요 반응 API (새로 추가)
    // 리액트에서 api.post(`/api/replies/${replyId}/reactions`) 로 접근할 때 여기로 들어옵니다.
    @PostMapping("/replies/{replyId}/reactions")
    public ResponseEntity<String> toggleReplyReaction(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long replyId,
            @RequestBody ReactionRequestDto requestDto) {

        replyService.toggleReplyReaction(userId, replyId, requestDto.getReactionType());
        return ResponseEntity.ok("답글 반응 처리 완료");
    }
}