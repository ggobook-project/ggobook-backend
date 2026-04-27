package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 🌟 수정: @Query 싹 지우고, @EntityGraph 로 N+1 문제 해결! 순수 메서드명으로 통일!
    @EntityGraph(attributePaths = {"replies"})
    Slice<Comment> findByEpisode_EpisodeIdOrderByCreatedAtDesc(Long episodeId, Pageable pageable);

    // [기존 유지] 마이페이지용: @Query 없이 순수 JPA 메서드 명명 규칙 사용
    Slice<Comment> findByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
}