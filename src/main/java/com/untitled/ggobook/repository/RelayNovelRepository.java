package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.RelayNovel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    @Query("SELECT n FROM RelayNovel n LEFT JOIN FETCH n.entries WHERE n.relayNovelId = :id")
    Optional<RelayNovel> findByIdWithEntries(@Param("id") Long id);

    // ==========================================
    //  3. 마이페이지 전용: 내가 참여한 릴레이 소설 조회 (추가된 부분)
    // ==========================================
    @Query("SELECT DISTINCT r FROM RelayNovel r LEFT JOIN r.entries e WHERE r.userId = :userId OR e.userId = :userId ORDER BY r.createdAt DESC")
    List<RelayNovel> findMyRelayNovels(@Param("userId") Long userId);

}