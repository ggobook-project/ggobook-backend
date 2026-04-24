package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Report;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.enums.ReportStatus;
import com.untitled.ggobook.domain.enums.SuspensionDuration;
import com.untitled.ggobook.repository.ReportRepository;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // ==========================================
    // [조회] 대기 중인 신고 건수 카운트 (대시보드 뱃지용)
    // ==========================================
    @Transactional(readOnly = true)
    public long countPendingReports() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }

    // ==========================================
    // [조회] 미처리 신고 목록 불러오기
    // ==========================================
    @Transactional(readOnly = true)
    public List<Report> getPendingReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING);
    }

    // ==========================================
    // [처리] 신고 승인 및 유저 정지 실행
    // ==========================================
    @Transactional
    public void approveReportAndSuspendUser(Long reportId, SuspensionDuration duration, String processReason) {
        // 1. 신고 내역 조회
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고 내역을 찾을 수 없습니다."));

        // 2. 신고 상태를 '해결됨(RESOLVED)'으로 변경하고 사유 기록
        report.resolveReport(processReason);

        // 3. 신고당한 유저(ReportedUser)를 찾아 정지 처리
        User reportedUser = report.getReportedUser();
        reportedUser.suspend(duration); // User 엔티티에 만들어둔 suspend 메서드 호출

        // JPA의 더티 체킹 덕분에 별도의 save 호출 없이도 자동으로 Update 쿼리가 나갑니다.
    }

    // ==========================================
    // [처리] 단순 신고 완료 처리 (유저 정지 X - 중복 신고 처리용)
    // ==========================================
    @Transactional
    public void resolveReportOnly(Long reportId, String processReason) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고 내역을 찾을 수 없습니다."));

        // 유저 정지 로직 없이, 신고 자체만 '해결됨' 상태로 변경
        report.resolveReport(processReason);
    }

    // ==========================================
    // [처리] 신고 반려 (허위 신고 기각)
    // ==========================================
    @Transactional
    public void rejectReport(Long reportId, String processReason) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고 내역을 찾을 수 없습니다."));

        report.rejectReport(processReason);
    }
}