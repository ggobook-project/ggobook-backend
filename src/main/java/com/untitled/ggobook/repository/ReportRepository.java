package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Report;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.enums.ReportStatus;
import com.untitled.ggobook.domain.enums.TargetType;
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

    @Query("SELECT r FROM Report r " +
            "JOIN FETCH r.reporter " +
            "JOIN FETCH r.reportedUser " +
            "WHERE r.status = :status AND r.targetType = :targetType " +
            "ORDER BY r.createdAt DESC")
    List<Report> findByStatusAndTargetTypeOrderByCreatedAtDesc(
            @Param("status") ReportStatus status,
            @Param("targetType") TargetType targetType);

    // 🌟 1. 특정 게시물(Target)에 대한 '미처리' 신고가 이미 존재하는지 확인
    // (사용자가 이미 신고한 게시물에 또 신고 버튼을 누르려 할 때 체크용)
    boolean existsByReporterAndTargetTypeAndTargetIdAndStatus(
            User reporter, TargetType targetType, Long targetId, ReportStatus status);

    // 🌟 2. 특정 게시물에 쌓인 '미처리' 신고가 총 몇 건인지 카운트
    // (관리자 목록에서 "이 글은 5번 신고됨"이라고 숫자를 보여줄 때 사용)
    long countByTargetTypeAndTargetIdAndStatus(TargetType targetType, Long targetId, ReportStatus status);

    // 🌟 3. 특정 게시물에 걸린 모든 '미처리' 신고 목록을 한꺼번에 가져오기
    // (관리자가 글 하나를 블라인드 처리할 때, 관련된 모든 신고를 한 방에 '처리 완료'로 바꿀 때 사용)
    List<Report> findByTargetTypeAndTargetIdAndStatus(TargetType targetType, Long targetId, ReportStatus status);

}
