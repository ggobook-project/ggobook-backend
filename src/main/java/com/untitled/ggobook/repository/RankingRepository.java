package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface RankingRepository extends JpaRepository<Content, Long> {

    // 1. 모든 작품의 랭킹 점수 초기화 (매일 계산 전 리셋)
    @Modifying
    @Transactional
    @Query("UPDATE Content c SET c.weeklyScore = 0.0")
    void resetAllWeeklyScores();

    // 2. 특정 작품의 '최근 7일간 회차 좋아요 수'
    @Query("SELECT COUNT(el) FROM EpisodeLike el WHERE el.episode.content.contentId = :contentId AND el.createdAt >= :startDate")
    long countRecentEpisodeLikes(@Param("contentId") Long contentId, @Param("startDate") LocalDateTime startDate);

    // 3. 특정 작품의 '최근 7일간 작품 찜(Likes) 수'
    @Query("SELECT COUNT(l) FROM Likes l WHERE l.content.contentId = :contentId AND l.createdAt >= :startDate")
    long countRecentContentLikes(@Param("contentId") Long contentId, @Param("startDate") LocalDateTime startDate);

    // 4. 특정 작품의 '최근 7일간 평균 별점' (별점이 없으면 3.0점 기본값 처리하여 0점 폭사 방지)
    @Query("SELECT COALESCE(AVG(r.score), 3.0) FROM Rating r WHERE r.episode.content.contentId = :contentId AND r.createdAt >= :startDate")
    double getRecentAverageRating(@Param("contentId") Long contentId, @Param("startDate") LocalDateTime startDate);
}