package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByEpisode_EpisodeIdAndUserId(Long episodeId, Long userId);

    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Rating r WHERE r.episode.episodeId = :episodeId")
    double getAverageScoreByEpisodeId(@Param("episodeId") Long episodeId);
}