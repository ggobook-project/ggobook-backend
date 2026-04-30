package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// 작품 리포지토리
import com.untitled.ggobook.domain.enums.Status;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {


    @Query("SELECT DISTINCT c FROM Content c " +
            "LEFT JOIN c.tags t " +
            "WHERE (:keyword IS NULL OR c.title LIKE %:keyword% OR t.tagName LIKE %:keyword%) " +
            "AND (:genre IS NULL OR c.genre = :genre) " +
            "AND (:type IS NULL OR c.type = :type) " +
            "ORDER BY c.createdAt DESC")
    Slice<Content> findContentList(@Param("keyword") String keyword,
                                   @Param("genre") String genre,
                                   @Param("type") String type,
                                   Pageable pageable);

    @Override
    Optional<Content> findById(Long contentId);

    // 특정 작가의 작품만 모아보는 기능이 필요할 때 사용합니다.
    List<Content> findByAuthorId(Long authorId);

    @Query("SELECT c FROM Content c " +
            "LEFT JOIN FETCH c.episodes " +
            "WHERE c.contentId = :contentId")
    Optional<Content> findByIdWithEpisodes(@Param("contentId") Long contentId);

    @EntityGraph(attributePaths = {"author"})
    List<Content> findByTypeOrderByContentIdDesc(String type);

    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT c FROM Content c WHERE c.contentId = :contentId")
    Optional<Content> findByIdWithAuthor(@Param("contentId") Long contentId);

    @Query("SELECT c FROM Content c LEFT JOIN FETCH c.author WHERE c.status IN (:statuses) ORDER BY c.createdAt DESC")
    List<Content> findByStatusInWithAuthor(@Param("statuses") List<Status> statuses);

    List<Content> findByTypeAndTitleContainingOrTypeAndAuthor_NicknameContainingOrderByContentIdDesc(
            String type1, String title, String type2, String nickname
    );

    @Query("SELECT c FROM Content c " +
            "WHERE c.type = :type " +
            "AND (:day IS NULL OR c.serialDay = :day) " +
            "ORDER BY c.contentId DESC")
    List<Content> findByTypeAndSerialDay(@Param("type") String type, @Param("day") String day);

    long countByStatus(Status status);

    Slice<Content> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

}