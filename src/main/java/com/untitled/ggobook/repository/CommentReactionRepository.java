package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Comment;
import com.untitled.ggobook.domain.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    Optional<CommentReaction> findByUserIdAndComment(Long userId, Comment comment);
    void deleteAllByComment(Comment comment);
}