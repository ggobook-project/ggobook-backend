package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.OwnedContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OwnedContentRepository extends JpaRepository<OwnedContent, Long> {
    boolean existsByUserIdAndEpisode(Long id, Episode episode);

    OwnedContent findByUserIdAndContentContentIdAndEpisodeEpisodeId(Long userId, Long contentId, Long episodeId);

    @Query("SELECT DISTINCT oc.content FROM OwnedContent oc WHERE oc.userId = :userId")
    List<Content> findOwnedContentsByUserId(@Param("userId") Long userId);

    @Query("SELECT oc FROM OwnedContent oc WHERE oc.userId = :userId AND oc.content.contentId = :contentId")
    List<OwnedContent> findOwnedEpisodesByUserIdAndContentId(@Param("userId") Long userId, @Param("contentId") Long contentId);
}
