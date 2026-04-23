package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // [웹툰 하단용] 특정 회차의 댓글을 부모-자식 관계(Reply)까지 한 번에 묶어서 최신순으로 가져옵니다.
    // LEFT JOIN FETCH를 써서 자식 답글이 없어도 부모 댓글을 정상적으로 가져옵니다.
    @Query("SELECT DISTINCT c FROM Comment c " +
            "LEFT JOIN FETCH c.replies " +
            "WHERE c.episode.episodeId = :episodeId " +
            "ORDER BY c.createdAt DESC")
    Slice<Comment> findCommentsWithRepliesByEpisodeId(@Param("episodeId") Long episodeId, Pageable pageable);

    // [마이페이지용] 내가 쓴 부모 댓글만 작품/회차 정보와 함께 가져옵니다.
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.content " +
            "JOIN FETCH c.episode " +
            "WHERE c.id = :id " +
            "ORDER BY c.createdAt DESC")
    Slice<Comment> findMyComments(@Param("id") Long id, Pageable pageable);
}