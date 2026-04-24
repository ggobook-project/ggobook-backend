package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Comment;
import com.untitled.ggobook.domain.Reply;
import com.untitled.ggobook.dto.ReplyRequestDto;
import com.untitled.ggobook.repository.CommentRepository;
import com.untitled.ggobook.repository.ReplyRepository;
// 🌟 팩트: 더 이상 유저 DB 조회가 필요 없으므로 UserRepository import 제거!
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    // 🌟 팩트: UserRepository 주입받던 부분 삭제 (클린 코드)

    // 1. 답글 작성
    @Transactional
    public void createReply(Long id, Long commentId, ReplyRequestDto requestDto) {
        // 유저 조회 쿼리 삭제 완료! 바로 부모 댓글만 찾습니다.
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));

        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글에는 답글을 달 수 없습니다.");
        }

        Reply reply = new Reply();
        reply.setComment(comment);
        reply.setUserId(id); // 🌟 컨트롤러에서 넘어온 PK(id)를 바로 꽂아 넣습니다.
        reply.setReplyText(requestDto.getReplyText());
        reply.setIsDeleted(false);

        replyRepository.save(reply);
    }

    // 2. 답글 삭제 (및 부모 연쇄 삭제 로직)
    @Transactional
    public void deleteReply(Long id, Long replyId) {
        // 유저 조회 쿼리 삭제 완료! 답글만 바로 찾습니다.
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("답글 없음"));

        // 🌟 컨트롤러에서 넘어온 PK(id)와 답글 주인의 PK를 다이렉트로 비교합니다.
        if (!reply.getUserId().equals(id)) {
            throw new IllegalArgumentException("본인의 답글만 삭제할 수 있습니다.");
        }

        Comment parentComment = reply.getComment();

        // 답글은 밑에 딸린 애가 없으므로 무조건 진짜 삭제!
        replyRepository.delete(reply);

        // 부모 댓글이 이미 '삭제된 댓글입니다' 상태였는데,
        // 방금 지운 이 답글이 마지막 남은 답글이었다면? 부모 껍데기도 이제 필요 없으니 DB에서 날려줍니다.
        if (parentComment.getIsDeleted() && parentComment.getReplies().size() == 1) {
            commentRepository.delete(parentComment);
        }
    }
}