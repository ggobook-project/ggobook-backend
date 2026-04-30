package com.untitled.ggobook.scheduler;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.RankingRepository;
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
public class RankingScheduler {

    private final ContentRepository contentRepository;
    private final RankingRepository rankingRepository;

    // 매일 새벽 2시 0분 0초에 실행 (크론 표현식)
    // 테스트하실 때는 "0 * * * * *" 로 바꾸면 매 1분마다 실행됩니다!
    // @Scheduled(cron = "*/10 * * * * *")
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void updateWeeklyRankingScores() {
        log.info("🔥 주간 랭킹 점수 업데이트 배치 작업 시작...");
        long startTime = System.currentTimeMillis();

        // 1. 기준 시간 설정 (정확히 지금으로부터 7일 전)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 2. 기존 점수 싹 리셋 (안 하면 과거 점수에 계속 더해짐)
        rankingRepository.resetAllWeeklyScores();

        // 3. 현재 승인(APPROVED)된 모든 작품 가져오기 (연재중/완결 무관)
        List<Content> allContents = contentRepository.findAll();

        // 4. 각 작품별로 7일치 데이터를 긁어와 점수 계산
        for (Content content : allContents) {
            Long contentId = content.getContentId();

            // A. 최신 좋아요 수 (가중치 x1)
            long recentEpisodeLikes = rankingRepository.countRecentEpisodeLikes(contentId, sevenDaysAgo);

            // B. 최신 작품 찜 수 (VIP 지표이므로 가중치 x3)
            long recentContentLikes = rankingRepository.countRecentContentLikes(contentId, sevenDaysAgo);

            // C. 최신 평균 별점 (작품성 버프)
            double recentAvgRating = rankingRepository.getRecentAverageRating(contentId, sevenDaysAgo);

            // D. 대망의 랭킹 수식!
            // (최근좋아요 + (최근찜 * 3)) * (평균별점 / 5.0)
            double rawScore = (recentEpisodeLikes + (recentContentLikes * 3)) * (recentAvgRating / 5.0);

            // 소수점 1자리까지만 깔끔하게 저장
            double finalScore = Math.round(rawScore * 10) / 10.0;

            content.setWeeklyScore(finalScore);
        }

        // JPA 더티체킹으로 트랜잭션 종료 시 DB 일괄 업데이트 됨
        long endTime = System.currentTimeMillis();
        log.info("✅ 주간 랭킹 점수 업데이트 완료. 소요 시간: {}ms", (endTime - startTime));
    }
}