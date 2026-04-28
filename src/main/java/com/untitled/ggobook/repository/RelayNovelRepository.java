package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.RelayNovel;
import com.untitled.ggobook.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RelayNovelRepository extends JpaRepository<RelayNovel, Long> {

    // ==========================================
    // 1. 최신순 조회 (상태가 PRIVATE이 아닌 것만!)
    // ==========================================
    // 🌟 메서드명에 'StatusNot'을 추가하면 JPA가 알아서 상태 필터링을 해줍니다.
    Page<RelayNovel> findByStatusNotOrderByCreatedAtDesc(Status status, Pageable pageable);

    // ==========================================
    // 2. 인기순 조회 (JPQL 수정)
    // ==========================================
    @Query("SELECT r FROM RelayNovel r " +
            "LEFT JOIN r.entries e " +
            "WHERE r.status <> :status " + // 🌟 PRIVATE 상태를 제외하는 조건 추가
            "GROUP BY r.relayNovelId " +
            "ORDER BY COUNT(e) DESC, r.title ASC")
    Page<RelayNovel> findAllOrderByEntryCountDescAndTitleAsc(
            @Param("status") Status status,
            Pageable pageable);

    @Query("SELECT n FROM RelayNovel n LEFT JOIN FETCH n.entries WHERE n.relayNovelId = :id")
    Optional<RelayNovel> findByIdWithEntries(@Param("id") Long id);

    // ==========================================
    //  3. 마이페이지 전용: 내가 참여한 릴레이 소설 조회 (추가된 부분)
    // ==========================================
    @Query("SELECT DISTINCT r FROM RelayNovel r LEFT JOIN r.entries e WHERE r.userId = :userId OR e.userId = :userId ORDER BY r.createdAt DESC")
    List<RelayNovel> findMyRelayNovels(@Param("userId") Long userId);

}