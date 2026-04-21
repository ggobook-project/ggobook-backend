package com.untitled.ggobook.scheduler;

import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.enums.UserStatus;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSuspensionScheduler {

    private final UserRepository userRepository;

    // ==========================================
    // [자동화] 정지 기간 만료 유저 복구 (매시간 정각 실행)
    // ==========================================
    @Scheduled(cron = "0 0 * * * *") // 초 분 시 일 월 요일
    @Transactional
    public void releaseSuspendedUsers() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 상태가 SUSPENDED이면서, 정지 종료 시간이 현재보다 이전인 유저들을 조회
        List<User> suspendedUsers = userRepository.findByStatusAndSuspensionEndDateBefore(
                UserStatus.SUSPENDED,
                now
        );

        if (suspendedUsers.isEmpty()) {
            return;
        }

        // 2. 대상 유저들의 상태를 ACTIVE로 변경
        for (User user : suspendedUsers) {
            user.activate(); // User 엔티티의 activate 메서드 호출
            log.info("정지 자동 해제 완료: 닉네임({}), 해제일시({})", user.getNickname(), now);
        }

        log.info("총 {}명의 유저가 활동 상태로 복구되었습니다.", suspendedUsers.size());
    }
}