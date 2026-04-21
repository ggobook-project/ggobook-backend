package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Report;
import com.untitled.ggobook.domain.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // 1. [대시보드용] 대기 중(PENDING)인 신고 갯수 카운트
    long countByStatus(ReportStatus status);

    // ==========================================
    // 🌟 [수정된 부분] N+1 문제 방지를 위한 Fetch Join 쿼리 작성
    // ==========================================
    // 신고 목록을 가져올 때(Report), 신고자(reporter)와 신고당한사람(reportedUser)의
    // 유저 정보를 한 방의 쿼리로 미리 다 당겨옵니다.
    @Query("SELECT r FROM Report r " +
            "JOIN FETCH r.reporter " +
            "JOIN FETCH r.reportedUser " +
            "WHERE r.status = :status " +
            "ORDER BY r.createdAt DESC")
    List<Report> findByStatusOrderByCreatedAtDesc(@Param("status") ReportStatus status);
}
