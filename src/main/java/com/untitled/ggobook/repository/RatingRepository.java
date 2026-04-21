package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Rating;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.content.contentId = :contentId")
    Double findAverageByContentId(@Param("contentId") Long contentId);


    Rating findByUserIdAndContent_ContentId(Long userId, Long contentId);

}
