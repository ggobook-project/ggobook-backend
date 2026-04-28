package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Reply;
import com.untitled.ggobook.domain.ReplyReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReplyReactionRepository extends JpaRepository<ReplyReaction, Long> {
    Optional<ReplyReaction> findByUserIdAndReply(Long userId, Reply reply);

    Optional<ReplyReaction> findByUserIdAndReply_ReplyId(Long userId, Long replyId);
    void deleteAllByReply(Reply reply);
}