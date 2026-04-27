package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.OwnedContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnedContentRepository extends JpaRepository<OwnedContent, Long> {
    boolean existsByUserIdAndEpisode(Long id, Episode episode);
}
