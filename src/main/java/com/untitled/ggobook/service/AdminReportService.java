package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Report;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.enums.ReportStatus;
import com.untitled.ggobook.domain.enums.SuspensionDuration;
import com.untitled.ggobook.domain.enums.TargetType;
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

    // 관리자 엔티티 조회 공통 메서드 (private이므로 @Transactional 생략 가능하지만 유지)
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

    // [처리] 신고 승인 및 유저 정지 실행 + 중복 신고 일괄 처리
    @Transactional
    public void approveReportAndSuspendUser(Long reportId, Long adminId, SuspensionDuration duration, String processReason) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고 내역을 찾을 수 없습니다."));

        User admin = getAdmin(adminId);

        // 1. 해당 신고 '해결됨' 처리
        report.resolveReport(admin, processReason);

        // 2. 신고당한 유저 정지 처리
        report.getReportedUser().suspend(duration);

        // 🌟 3. 일괄 처리: 동일한 게시물에 대한 다른 모든 미처리 신고도 같이 해결
        resolveDuplicateReports(report.getTargetType(), report.getTargetId(), admin, "[일괄 처리] " + processReason);
    }

    // [처리] 단순 신고 완료 처리 + 중복 신고 일괄 처리
    @Transactional
    public void resolveReportOnly(Long reportId, Long adminId, String processReason) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고 내역을 찾을 수 없습니다."));

        User admin = getAdmin(adminId);

        report.resolveReport(admin, processReason);

        // 🌟 동일 게시물 중복 신고 일괄 해결
        resolveDuplicateReports(report.getTargetType(), report.getTargetId(), admin, "[일괄 처리] " + processReason);
    }

    // [처리] 신고 반려 (허위 신고 기각)
    @Transactional
    public void rejectReport(Long reportId, Long adminId, String processReason) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고 내역을 찾을 수 없습니다."));

        User admin = getAdmin(adminId);

        // 반려 시에는 보통 해당 건만 기각합니다. (다른 사람의 신고는 정당할 수 있으므로)
        report.rejectReport(admin, processReason);
    }

    // 🌟 [내부 메서드] 중복 신고 일괄 해결 로직
    private void resolveDuplicateReports(TargetType type, Long targetId, User admin, String reason) {
        List<Report> duplicates = reportRepository.findByTargetTypeAndTargetIdAndStatus(
                type, targetId, ReportStatus.PENDING);

        for (Report dupe : duplicates) {
            dupe.resolveReport(admin, reason);
        }
    }
}