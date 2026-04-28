package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    // 1. 특정 작품(Content)에 내가 남긴 별점 찾기
    // 주의: 엔티티에서 유저 ID 필드명을 'id'로 선언하셨으므로 AndId 로 작성합니다.
    Optional<Rating> findByContent_ContentIdAndId(Long contentId, Long userId);

    // 2. 특정 작품의 평균 별점 구하기 (데이터가 하나도 없으면 0.0 반환)
    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Rating r WHERE r.content.contentId = :contentId")
    double getAverageScoreByContentId(@Param("contentId") Long contentId);
}