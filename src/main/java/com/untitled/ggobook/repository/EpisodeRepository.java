package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    // 1. [기본 노출용] 최신화부터 보기 (내림차순: Desc)
    List<Episode> findByContentOrderByEpisodeNumberDesc(Content content);

    // 2. [검수 기능] 특정 상태인 회차들만 조회
    List<Episode> findByStatus(Status status);

    // 3. [스케줄러 핵심] 예약 시간(scheduledAt)이 '지금'을 지난 승인된 회차들 조회
    List<Episode> findByStatusAndScheduledAtBefore(Status status, LocalDateTime now);

    // ✅ [추가] 상세 조회 시 지연 로딩(Lazy) 에러를 방지하기 위해
    // 소설 본문과 웹툰 이미지를 한 번에 가져오는 쿼리입니다.
    @Query("SELECT e FROM Episode e " +
            "LEFT JOIN FETCH e.novel " +
            "LEFT JOIN FETCH e.comicToons " +
            "JOIN FETCH e.content " +
            "WHERE e.episodeId = :episodeId")
    Optional<Episode> findByIdWithDetails(@Param("episodeId") Long episodeId);


    @Query("SELECT e FROM Episode e " +
            "WHERE e.content.contentId = :contentId " +
            "AND e.status = :currentNeedStatus " +
            "AND e.scheduledAt > CURRENT_TIMESTAMP " +
            "ORDER BY e.episodeNumber DESC")
    Slice<Episode> findEpisodeListByContentId(
            @Param("contentId")Long contentId,
            Pageable pageable,
            @Param("currentNeedStatus") String currentNeedStatus);
}
