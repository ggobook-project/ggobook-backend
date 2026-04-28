package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.*; // 🌟 ReactionRequestDto 등을 포함하기 위해 전체 import
import com.untitled.ggobook.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 1. 특정 회차의 댓글 목록 조회 (기존 유지)
    @GetMapping("/episodes/{episodeId}/comments")
    public ResponseEntity<Slice<CommentResponseDto>> getEpisodeComments(
            @AuthenticationPrincipal Long userId, // 🌟 추가: 현재 로그인한 사람의 ID를 받습니다. (비회원이면 알아서 null이 들어갑니다)
            @PathVariable Long episodeId,
            Pageable pageable) {

        // 🌟 수정: 서비스로 넘길 때 userId도 같이 넘겨줍니다!
        Slice<CommentResponseDto> response = commentService.getEpisodeComments(userId, episodeId, pageable);
        return ResponseEntity.ok(response);
    }

    // 2. 내가 쓴 댓글 목록 조회 (기존 유지)
    @GetMapping("/my/comments")
    public ResponseEntity<Slice<MyCommentDto>> getMyCommentList(
            @AuthenticationPrincipal Long id,
            Pageable pageable) {
        Slice<MyCommentDto> response = commentService.getMyComments(id, pageable);
        return ResponseEntity.ok(response);
    }

    // 3. 댓글 작성 (기존 유지)
    @PostMapping("/episodes/{episodeId}/comments")
    public ResponseEntity<String> createComment(
            @AuthenticationPrincipal Long id,
            @PathVariable Long episodeId,
            @RequestBody CommentRequestDto requestDto) {
        commentService.createComment(id, episodeId, requestDto);
        return ResponseEntity.ok("댓글이 성공적으로 작성되었습니다.");
    }

    // 4. 댓글 삭제 (기존 유지)
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteMyComment(
            @AuthenticationPrincipal Long id,
            @PathVariable Long commentId) {
        commentService.deleteComment(id, commentId);
        return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
    }

    // 5. 댓글 수정 (기존 유지)
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<String> updateComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto) {
        commentService.updateComment(userId, commentId, requestDto);
        return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
    }

    // 🌟 6. [추가] 부모 댓글 좋아요/싫어요 토글 API
    @PostMapping("/comments/{commentId}/reactions")
    public ResponseEntity<String> toggleCommentReaction(
            @AuthenticationPrincipal Long id,
            @PathVariable Long commentId,
            @RequestBody ReactionRequestDto requestDto) {
        commentService.toggleCommentReaction(id, commentId, requestDto.getReactionType());
        return ResponseEntity.ok("댓글 반응 처리 완료");
    }
}