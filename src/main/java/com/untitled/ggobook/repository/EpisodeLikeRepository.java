package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.EpisodeLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EpisodeLikeRepository extends JpaRepository<EpisodeLike, Long> {
    Optional<EpisodeLike> findByUserIdAndEpisode(Long userId, Episode episode);
}