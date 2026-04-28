package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Report;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.enums.ReportStatus;
import com.untitled.ggobook.dto.ReportRequestDTO;
import com.untitled.ggobook.repository.ReportRepository;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createReport(Long reporterId, ReportRequestDTO dto) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        User reportedUser = userRepository.findById(dto.getReportedUserId())
                .orElseThrow(() -> new IllegalArgumentException("피신고자를 찾을 수 없습니다."));

        // 🌟 중복 신고 체크: 이미 동일한 건에 대해 PENDING 상태인 신고가 있는지 확인
        boolean alreadyReported = reportRepository.existsByReporterAndTargetTypeAndTargetIdAndStatus(
                reporter, dto.getTargetType(), dto.getTargetId(), ReportStatus.PENDING);

        if (alreadyReported) {
            throw new IllegalStateException("이미 해당 게시물에 대해 접수된 신고가 있습니다.");
        }

        // 🌟 신고 엔티티 생성 (아까 만든 Builder 활용)
        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .targetId(dto.getTargetId())
                .targetType(dto.getTargetType())
                .reportReason(dto.getReportReason())
                .build();

        reportRepository.save(report);
    }
}