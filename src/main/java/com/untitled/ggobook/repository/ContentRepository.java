package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Content;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// 작품 리포지토리
public interface ContentRepository extends JpaRepository<Content, Long> {

    @Query("SELECT c FROM Content c " +
            "WHERE (:keyword IS NULL OR c.title LIKE %:keyword%) " +
            "AND (:genre IS NULL OR c.genre = :genre)" +
            "AND (:type IS NULL OR c.type = :type)" +
            "ORDER BY c.createdAt DESC")
    Slice<Content> findContentList(@Param("keyword") String keyword,
                                   @Param("genre") String genre,
                                   @Param("type") String type,
                                   Pageable pageable);

    @Override
    Optional<Content> findById(Long contentId);


}
