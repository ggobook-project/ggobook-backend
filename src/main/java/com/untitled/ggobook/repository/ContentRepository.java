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

    // 기획서의 getInspectionList() 기능을 위한 메서드입니다.
    // 검수 대기(PENDING) 상태인 작품들만 DB에서 쏙 뽑아옵니다.
    // (JPA가 내부적으로 "SELECT * FROM content WHERE status = 'PENDING'" 쿼리를 실행합니다.)
    List<Content> findByStatus(Status status);

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