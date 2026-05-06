package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.enums.Status;
import org.springframework.data.domain.Page; // 🌟 Page 임포트
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Query("SELECT DISTINCT c FROM Content c " +
            "LEFT JOIN c.tags t " +
            "WHERE (:keyword IS NULL OR c.title LIKE %:keyword% OR t.tagName LIKE %:keyword%) " +
            "AND (:genre IS NULL OR c.genre = :genre) " +
            "AND (:type IS NULL OR c.type = :type) " +
            "AND (:serialDay IS NULL OR c.serialDay = :serialDay) " +
            "ORDER BY c.createdAt DESC")
    Slice<Content> findContentList(@Param("keyword") String keyword,
                                   @Param("genre") String genre,
                                   @Param("type") String type,
                                   @Param("serialDay") String serialDay,
                                   Pageable pageable);

    @Override
    Optional<Content> findById(Long contentId);

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

    @Query("SELECT c FROM Content c " +
            "WHERE (:keyword IS NULL OR c.title LIKE %:keyword%) " +
            "AND (:genre IS NULL OR c.genre = :genre)" +
            "AND (:type IS NULL OR c.type = :type)" +
            "AND (:serialDay IS NULL OR c.serialDay = :serialDay) " +
            "ORDER BY c.weeklyScore DESC, c.createdAt DESC")
    Slice<Content> findPopularContentList(@Param("keyword") String keyword,
                                          @Param("genre") String genre,
                                          @Param("type") String type,
                                          @Param("serialDay") String serialDay,
                                          Pageable pageable);

    Slice<Content> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    // ==========================================
    // 🌟 [추가] 관리자 작품 관리 페이지 전용 Pageable 쿼리
    // DB에서부터 키워드, 요일 필터링을 거쳐 딱 10개만 가져오고 총 페이지 수를 계산합니다.
    // ==========================================
    @Query("SELECT c FROM Content c " +
            "LEFT JOIN c.author a " +
            "WHERE c.type = :type " +
            "AND (:day IS NULL OR c.serialDay = :day) " +
            "AND (:keyword IS NULL OR c.title LIKE %:keyword% OR a.nickname LIKE %:keyword%)")
    Page<Content> findAdminContentsWithPaging(
            @Param("type") String type,
            @Param("keyword") String keyword,
            @Param("day") String day,
            Pageable pageable
    );
}