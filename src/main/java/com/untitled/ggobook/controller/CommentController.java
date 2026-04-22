package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.CommentRequestDto;
import com.untitled.ggobook.dto.CommentResponseDto;
import com.untitled.ggobook.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 1. 특정 회차의 댓글 목록 조회 (웹툰 뷰 하단용)
    @GetMapping("/api/episodes/{episodeId}/comments")
    public ResponseEntity<Slice<CommentResponseDto>> getComments(
            @PathVariable Long episodeId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getEpisodeComments(episodeId, pageable));
    }

    // 2. 새 부모 댓글 작성
    @PostMapping("/api/episodes/{episodeId}/comments")
    public ResponseEntity<String> createComment(
            @AuthenticationPrincipal String userId,
            @PathVariable Long episodeId,
            @RequestBody CommentRequestDto requestDto) {
        commentService.createComment(userId, episodeId, requestDto);
        return ResponseEntity.ok("댓글이 등록되었습니다.");
    }

    // 3. 댓글 삭제 (마이페이지/웹툰 하단 공용)
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @AuthenticationPrincipal String userId,
            @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }
}