package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Comment;
import com.untitled.ggobook.domain.Reply;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.dto.ReplyRequestDto;
import com.untitled.ggobook.repository.CommentRepository;
import com.untitled.ggobook.repository.ReplyRepository;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // 1. 답글 작성
    @Transactional
    public void createReply(String loginId, Long commentId, ReplyRequestDto requestDto) {
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));

        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글에는 답글을 달 수 없습니다.");
        }

        Reply reply = new Reply();
        reply.setComment(comment);
        reply.setUserId(user.getId());
        reply.setReplyText(requestDto.getReplyText());
        reply.setIsDeleted(false);

        replyRepository.save(reply);
    }

    // 2. 답글 삭제 (및 부모 연쇄 삭제 로직)
    @Transactional
    public void deleteReply(String loginId, Long replyId) {
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("답글 없음"));

        if (!reply.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 답글만 삭제할 수 있습니다.");
        }

        Comment parentComment = reply.getComment();

        // 답글은 밑에 딸린 애가 없으므로 무조건 진짜 삭제!
        replyRepository.delete(reply);

        // 🌟 클린 코드의 디테일: 만약 부모 댓글이 이미 '삭제된 댓글입니다' 상태였는데,
        // 방금 지운 이 답글이 마지막 남은 답글이었다면? 부모 껍데기도 이제 필요 없으니 DB에서 시원하게 날려줍니다.
        if (parentComment.getIsDeleted() && parentComment.getReplies().size() == 1) {
            commentRepository.delete(parentComment);
        }
    }
}