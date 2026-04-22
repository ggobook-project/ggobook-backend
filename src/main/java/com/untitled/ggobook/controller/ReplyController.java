package com.untitled.ggobook.controller;

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

    // 1. 답글 작성
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<String> createReply(
            @AuthenticationPrincipal String userId,
            @PathVariable Long commentId,
            @RequestBody ReplyRequestDto requestDto) {
        replyService.createReply(userId, commentId, requestDto);
        return ResponseEntity.ok("답글이 등록되었습니다.");
    }

    // 2. 답글 삭제 (마이페이지/웹툰 하단 공용)
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<String> deleteReply(
            @AuthenticationPrincipal String userId,
            @PathVariable Long replyId) {
        replyService.deleteReply(userId, replyId);
        return ResponseEntity.ok("답글이 삭제되었습니다.");
    }
}