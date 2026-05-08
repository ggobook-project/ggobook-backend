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
    private final EpisodeRepository episodeRepository; // 🌟 핵심 수술 1: 회차 창고(Repository) 접근 권한 추가!
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardData() {
        // 🌟 핵심 수술 2: 작품 대기 개수와 회차 대기 개수를 각각 가져와서 합칩니다!
        long contentPendingCount = contentRepository.countByStatus(Status.PENDING);
        long episodePendingCount = episodeRepository.countByStatus(Status.PENDING);
        long totalInspectionCount = contentPendingCount + episodePendingCount; // 총합 계산

        // 2. 신고 접수 (처리되지 않은 신고 예시)
        long reportCount = reportRepository.countByStatus(ReportStatus.PENDING);

        // 3. 전체 회원 (User 역할인 사람만)
        long totalUserCount = userRepository.countByRole("ROLE_USER");

        // 4. 오늘 가입자 (현재 시간 기준 오늘 0시부터)
        LocalDateTime startOfToday = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayJoinCount = userRepository.countByCreatedAtAfter(startOfToday);

        return AdminDashboardDTO.builder()
                .pendingInspectionCount(totalInspectionCount) // 🌟 합산된 전체 대기 개수 전달
                .reportCount(reportCount)
                .totalUserCount(totalUserCount)
                .todayJoinCount(todayJoinCount)
                .inspectionBadge((int) totalInspectionCount) // 🌟 메뉴 뱃지도 통합 개수로 띄움!
                .reportBadge((int) reportCount)
                .build();
    }
}