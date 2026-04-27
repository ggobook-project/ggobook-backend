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

    // 관리자 엔티티 조회 공통 메서드
    @Transactional
    private User getAdmin(Long adminId) {
        return userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));
    }

    // [조회] 대기 중인 신고 건수 카운트
    @Transactional(readOnly = true)
    public long countPendingReports() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }

    // [조회] 미처리 신고 목록 불러오기
    @Transactional(readOnly = true)
    public List<Report> getPendingReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING);
    }

    // [처리] 신고 승인 및 유저 정지 실행
    @Transactional
    public void approveReportAndSuspendUser(Long reportId, Long adminId, SuspensionDuration duration, String processReason) {
        // 1. 신고 내역 조회
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고 내역을 찾을 수 없습니다."));

        // 2. 관리자 조회
        User admin = getAdmin(adminId);

        // 3. 신고 상태를 '해결됨(RESOLVED)'으로 변경하고 관리자 ID 및 사유 기록
        report.resolveReport(admin, processReason);

        // 4. 신고당한 유저 정지 처리
        report.getReportedUser().suspend(duration);
    }

    // [처리] 단순 신고 완료 처리
    @Transactional
    public void resolveReportOnly(Long reportId, Long adminId, String processReason) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고 내역을 찾을 수 없습니다."));

        User admin = getAdmin(adminId);

        // 신고 자체만 '해결됨' 상태로 변경 및 관리자 기록
        report.resolveReport(admin, processReason);
    }

    // [처리] 신고 반려 (허위 신고 기각)
    @Transactional
    public void rejectReport(Long reportId, Long adminId, String processReason) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고 내역을 찾을 수 없습니다."));

        User admin = getAdmin(adminId);

        // 허위 신고 기각 처리 및 관리자 기록
        report.rejectReport(admin, processReason);
    }
}