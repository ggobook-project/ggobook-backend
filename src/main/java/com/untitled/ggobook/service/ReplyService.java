package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.*;
import com.untitled.ggobook.domain.enums.ReactionType;
import com.untitled.ggobook.dto.ReplyRequestDto;
import com.untitled.ggobook.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final ReplyReactionRepository replyReactionRepository;
    private final CommentReactionRepository commentReactionRepository;

    // 🌟 핵심 추가: 유저 객체를 찾기 위해 요원을 투입합니다.
    private final UserRepository userRepository;

    @Transactional
    public void createReply(Long id, Long commentId, ReplyRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));

        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글에는 답글을 달 수 없습니다.");
        }

        // 🌟 핵심 수술: ID 숫자만 넣던 방식에서, 진짜 유저 객체를 찾아서 넣어주는 방식으로 변경!
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Reply reply = new Reply();
        reply.setComment(comment);

        // 🌟 이제 숫자가 아닌 객체를 통째로 연결합니다.
        reply.setUser(user);

        reply.setReplyText(requestDto.getReplyText());
        reply.setIsDeleted(false);

        replyRepository.save(reply);
    }

    @Transactional
    public void deleteReply(Long userId, Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("답글 없음"));

        // 🌟 수술: 이제 reply.getUserId()가 아니라 reply.getUser().getId()로 비교합니다.
        if (!reply.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 답글만 삭제할 수 있습니다.");
        }

        Comment parentComment = reply.getComment();
        replyReactionRepository.deleteAllByReply(reply);
        parentComment.getReplies().remove(reply);
        replyRepository.delete(reply);

        if (parentComment.getIsDeleted() && parentComment.getReplies().isEmpty()) {
            commentReactionRepository.deleteAllByComment(parentComment);
            commentRepository.delete(parentComment);
        }
    }

    @Transactional
    public void updateReply(Long userId, Long replyId, ReplyRequestDto requestDto) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("답글을 찾을 수 없습니다."));

        // 🌟 수술: 여기도 마찬가지로 객체에서 ID를 꺼내 비교합니다.
        if (!reply.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 답글만 수정할 수 있습니다.");
        }

        reply.setReplyText(requestDto.getReplyText());
    }

    @Transactional
    public void toggleReplyReaction(Long userId, Long replyId, ReactionType newReactionType) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("답글을 찾을 수 없습니다."));

        Optional<ReplyReaction> existing = replyReactionRepository.findByUserIdAndReply_ReplyId(userId, replyId);

        if (existing.isEmpty()) {
            replyReactionRepository.save(ReplyReaction.builder()
                    .userId(userId).reply(reply).reactionType(newReactionType).build());
            if (newReactionType == ReactionType.LIKE) reply.setLikeCount(reply.getLikeCount() + 1);
            else reply.setDislikeCount(reply.getDislikeCount() + 1);
        } else {
            ReplyReaction reaction = existing.get();
            if (reaction.getReactionType() == newReactionType) {
                replyReactionRepository.delete(reaction);
                if (newReactionType == ReactionType.LIKE) reply.setLikeCount(reply.getLikeCount() - 1);
                else reply.setDislikeCount(reply.getDislikeCount() - 1);
            } else {
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