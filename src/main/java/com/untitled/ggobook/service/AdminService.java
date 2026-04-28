package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.enums.ReportStatus;
import com.untitled.ggobook.domain.enums.Status;
import com.untitled.ggobook.dto.AdminDashboardDTO;
import com.untitled.ggobook.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardData() {
        // 1. 검수 대기 (Status.PENDING)
        long inspectionCount = contentRepository.countByStatus(Status.PENDING);

        // 2. 신고 접수 (처리되지 않은 신고 예시)
        long reportCount = reportRepository.countByStatus(ReportStatus.PENDING);

        // 3. 전체 회원 (User 역할인 사람만)
        long totalUserCount = userRepository.countByRole("ROLE_USER");

        // 4. 오늘 가입자 (현재 시간 기준 오늘 0시부터)
        LocalDateTime startOfToday = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayJoinCount = userRepository.countByCreatedAtAfter(startOfToday);

        return AdminDashboardDTO.builder()
                .pendingInspectionCount(inspectionCount)
                .reportCount(reportCount)
                .totalUserCount(totalUserCount)
                .todayJoinCount(todayJoinCount)
                .inspectionBadge((int) inspectionCount)
                .reportBadge((int) reportCount)
                .build();
    }
}