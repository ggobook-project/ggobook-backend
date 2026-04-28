package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Comment;
import com.untitled.ggobook.domain.Reply;
import com.untitled.ggobook.domain.ReplyReaction;
import com.untitled.ggobook.domain.enums.ReactionType;
import com.untitled.ggobook.dto.ReplyRequestDto;
import com.untitled.ggobook.repository.CommentReactionRepository;
import com.untitled.ggobook.repository.CommentRepository;
import com.untitled.ggobook.repository.ReplyReactionRepository;
import com.untitled.ggobook.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final ReplyReactionRepository replyReactionRepository; // 🌟 추가됨
    private final CommentReactionRepository commentReactionRepository;

    @Transactional
    public void createReply(Long id, Long commentId, ReplyRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));

        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글에는 답글을 달 수 없습니다.");
        }

        Reply reply = new Reply();
        reply.setComment(comment);
        reply.setUserId(id);
        reply.setReplyText(requestDto.getReplyText());
        reply.setIsDeleted(false);

        replyRepository.save(reply);
    }

    @Transactional
    public void deleteReply(Long userId, Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("답글 없음"));

        if (!reply.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 답글만 삭제할 수 있습니다.");
        }

        Comment parentComment = reply.getComment();

        // 1. 답글에 달린 좋아요 기록 청소
        replyReactionRepository.deleteAllByReply(reply);

        // 🌟 [핵심 마법] 부모 댓글의 자식 목록에서 이 답글의 손을 먼저 놓아줍니다. (JPA 연관관계 끊기)
        parentComment.getReplies().remove(reply);

        // 2. 손을 놨으니 이제 답글을 진짜로 지웁니다. (이때 좀비처럼 안 살아남습니다!)
        replyRepository.delete(reply);

        // 3. 만약 부모가 '삭제된댓글' 상태였고, 방금 손을 놓아서 남은 자식이 0개가 되었다면?
        if (parentComment.getIsDeleted() && parentComment.getReplies().isEmpty()) {
            // 부모의 좋아요 기록 청소 후 부모 껍데기도 완전 삭제!
            commentReactionRepository.deleteAllByComment(parentComment);
            commentRepository.delete(parentComment);
        }
    }

    // 🌟 1. 답글 수정 로직 (새로 추가)
    @Transactional
    public void updateReply(Long userId, Long replyId, ReplyRequestDto requestDto) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("답글을 찾을 수 없습니다."));

        if (!reply.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 답글만 수정할 수 있습니다.");
        }

        // JPA 더티 체킹: 값만 바꿔주면 트랜잭션 종료 시 알아서 DB 업데이트됨
        reply.setReplyText(requestDto.getReplyText());
    }

    // 🌟 2. 답글 좋아요/싫어요 토글 로직 (새로 추가)
    @Transactional
    public void toggleReplyReaction(Long userId, Long replyId, ReactionType newReactionType) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("답글을 찾을 수 없습니다."));

        // 유저가 이 답글에 이미 남긴 반응이 있는지 확인
        Optional<ReplyReaction> existing = replyReactionRepository.findByUserIdAndReply_ReplyId(userId, replyId);

        if (existing.isEmpty()) {
            // 처음 누름
            replyReactionRepository.save(ReplyReaction.builder()
                    .userId(userId).reply(reply).reactionType(newReactionType).build());
            if (newReactionType == ReactionType.LIKE) reply.setLikeCount(reply.getLikeCount() + 1);
            else reply.setDislikeCount(reply.getDislikeCount() + 1);
        } else {
            ReplyReaction reaction = existing.get();
            if (reaction.getReactionType() == newReactionType) {
                // 똑같은 거 또 누름 -> 취소 (0으로 돌아감)
                replyReactionRepository.delete(reaction);
                if (newReactionType == ReactionType.LIKE) reply.setLikeCount(reply.getLikeCount() - 1);
                else reply.setDislikeCount(reply.getDislikeCount() - 1);
            } else {
                // 반대 버튼 누름 -> 색깔/숫자 스위칭
                reaction.changeReaction(newReactionType);
                if (newReactionType == ReactionType.LIKE) {
                    reply.setDislikeCount(reply.getDislikeCount() - 1);
                    reply.setLikeCount(reply.getLikeCount() + 1);
                } else {
                    reply.setLikeCount(reply.getLikeCount() - 1);
                    reply.setDislikeCount(reply.getDislikeCount() + 1);
                }
            }
        }
    }
}