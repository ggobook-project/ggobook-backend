package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.MemberSuspend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberSuspendRepository extends JpaRepository<MemberSuspend, Long> {

    // 유저별 정지 내역 조회
    List<MemberSuspend> findByUserId(Long userId);

    // 현재 유효한 정지 내역 조회
    @Query("SELECT m FROM MemberSuspend m WHERE m.user.id = :userId AND m.endDate > :now ORDER BY m.endDate DESC LIMIT 1")
    Optional<MemberSuspend> findActiveSuspend(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}