package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.MemberSuspend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberSuspendRepository extends JpaRepository<MemberSuspend, Long> {

    // 유저별 정지 내역 조회
    List<MemberSuspend> findByUserId(Long userId);

    // 🌟 쿼리 시한폭탄 제거: Spring Data JPA 표준 네이밍 규칙으로 교체 (알아서 최신 1개만 가져옴)
    Optional<MemberSuspend> findFirstByUserIdAndEndDateAfterOrderByEndDateDesc(Long userId, LocalDateTime now);
}