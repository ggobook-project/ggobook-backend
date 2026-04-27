package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.*;
import com.untitled.ggobook.domain.enums.ReactionType;
import com.untitled.ggobook.dto.*;
import com.untitled.ggobook.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final EpisodeRepository episodeRepository;
    private final UserRepository userRepository;
    private final CommentReactionRepository commentReactionRepository; // 🌟 추가됨
    private final ReplyReactionRepository replyReactionRepository;

    // 1. 웹툰 하단 댓글 목록 조회 (기존 유지)
    @Transactional(readOnly = true)
    public Slice<CommentResponseDto> getEpisodeComments(Long userId, Long episodeId, Pageable pageable) {
        return commentRepository.findByEpisode_EpisodeIdOrderByCreatedAtDesc(episodeId, pageable)
                .map(comment -> {
                    // 1. 기존 로직으로 DTO 생성
                    CommentResponseDto dto = CommentResponseDto.from(comment);

                    // 2. 로그인 유저라면 방명록을 확인해서 상태(myReaction)를 덧입힘
                    if (userId != null) {
                        // 부모 댓글 확인
                        commentReactionRepository.findByUserIdAndComment(userId, comment)
                                .ifPresent(r -> dto.setMyReaction(r.getReactionType().name()));

                        // 자식 답글들 확인
                        dto.getReplies().forEach(replyDto -> {
                            // reply 엔티티를 찾기 위해 ID로 조회하는 레파지토리 메서드 필요
                            replyReactionRepository.findByUserIdAndReply_ReplyId(userId, replyDto.getReplyId())
                                    .ifPresent(reaction -> replyDto.setMyReaction(reaction.getReactionType().name()));
                        });
                    }
                    return dto;
                });
    }

    // 2. 내가 쓴 댓글 목록 조회 (기존 유지)
    @Transactional(readOnly = true)
    public Slice<MyCommentDto> getMyComments(Long userId, Pageable pageable) {
        Slice<Comment> comments = commentRepository.findByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable);
        return comments.map(comment -> new MyCommentDto(
                comment.getCommentId(),
                comment.getCommentText(),
                comment.getContent() != null && comment.getContent().getTitle() != null ? comment.getContent().getTitle() : "제목 없음",
                comment.getContent() != null ? comment.getContent().getContentId() : null,
                comment.getEpisode() != null ? comment.getEpisode().getEpisodeId() : null,
                comment.getEpisode() != null ? comment.getEpisode().getEpisodeNumber() + "화" : "1화",
                comment.getCreatedAt() != null ? comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) : "",
                comment.getContent() != null && comment.getContent().getType() != null ? comment.getContent().getType() : "WEBTOON"
        ));
    }

    // 3. 새 댓글 작성 (기존 유지)
    @Transactional
    public void createComment(Long userId, Long episodeId, CommentRequestDto requestDto) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회차입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setContent(episode.getContent());
        comment.setEpisode(episode);
        comment.setCommentText(requestDto.getCommentText());
        comment.setIsSpoiler(requestDto.getIsSpoiler() != null ? requestDto.getIsSpoiler() : false);
        comment.setIsDeleted(false);
        commentRepository.save(comment);
    }

    // 4. 댓글 삭제 (기존 유지)
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 댓글만 삭제할 수 있습니다.");
        }

        if (comment.getReplies().isEmpty()) {
            // 🌟 일반 댓글을 완전 삭제할 때도 '좋아요' 기록부터 청소!
            commentReactionRepository.deleteAllByComment(comment);
            commentRepository.delete(comment);
        } else {
            comment.setIsDeleted(true);
            comment.setCommentText("삭제된 댓글입니다.");
        }
    }

    // 5. 댓글 수정 로직 (기존 유지)
    @Transactional
    public void updateComment(Long userId, Long commentId, CommentRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }
        comment.setCommentText(requestDto.getCommentText());
    }

    // 🌟 6. [추가] 부모 댓글 좋아요/싫어요 토글 핵심 로직
    @Transactional
    public void toggleCommentReaction(Long userId, Long commentId, ReactionType newReactionType) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 찾을 수 없음"));

        Optional<CommentReaction> existing = commentReactionRepository.findByUserIdAndComment(userId, comment);

        if (existing.isEmpty()) {
            // 처음 반응함
            commentReactionRepository.save(CommentReaction.builder()
                    .userId(userId).comment(comment).reactionType(newReactionType).build());
            if (newReactionType == ReactionType.LIKE) comment.setLikeCount(comment.getLikeCount() + 1);
            else comment.setDislikeCount(comment.getDislikeCount() + 1);
        } else {
            CommentReaction reaction = existing.get();
            if (reaction.getReactionType() == newReactionType) {
                // 같은 반응 또 누름 -> 취소
                commentReactionRepository.delete(reaction);
                if (newReactionType == ReactionType.LIKE) comment.setLikeCount(comment.getLikeCount() - 1);
                else comment.setDislikeCount(comment.getDislikeCount() - 1);
            } else {
                // 반대 반응 누름 -> 교체
                reaction.changeReaction(newReactionType);
                if (newReactionType == ReactionType.LIKE) {
                    comment.setDislikeCount(comment.getDislikeCount() - 1);
                    comment.setLikeCount(comment.getLikeCount() + 1);
                } else {
                    comment.setLikeCount(comment.getLikeCount() - 1);
                    comment.setDislikeCount(comment.getDislikeCount() + 1);
                }
            }
        }
    }
}