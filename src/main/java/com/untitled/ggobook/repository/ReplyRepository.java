package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Reply;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    // [마이페이지용] 내가 쓴 자식 답글만 부모 댓글, 작품, 회차 정보와 함께 싹 가져옵니다.
    @Query("SELECT r FROM Reply r " +
            "JOIN FETCH r.comment c " +
            "JOIN FETCH c.content " +
            "JOIN FETCH c.episode " +
            "WHERE r.userId = :userId " +
            "ORDER BY r.createdAt DESC")
    Slice<Reply> findMyReplies(@Param("userId") Long userId, Pageable pageable);
}