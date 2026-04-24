package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.RelayNovel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RelayNovelRepository extends JpaRepository<RelayNovel, Long> {

    // ==========================================
    // 1. 최신순 조회 (JPA 기본 제공 네이밍 규칙)
    // ==========================================
    Page<RelayNovel> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // ==========================================
    // 2. 인기순 조회 (직접 작성한 JPQL)
    // ==========================================
    @Query("SELECT r FROM RelayNovel r " +
            "LEFT JOIN r.entries e " +
            "GROUP BY r.relayNovelId " +
            "ORDER BY COUNT(e) DESC, r.title ASC")
    Page<RelayNovel> findAllOrderByEntryCountDescAndTitleAsc(Pageable pageable);

}