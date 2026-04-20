package com.untitled.ggobook.scheduler;

import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Status;
import com.untitled.ggobook.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j // 로그를 찍기 위한 어노테이션
@Component // 스프링이 이 클래스를 관리하도록 등록
@RequiredArgsConstructor
public class EpisodeScheduler {

    private final EpisodeRepository episodeRepository;

    // cron: 초 분 시 일 월 요일 (여기서는 "매 1분마다" 실행되도록 설정)
    // 예: "0 0 0 * * *" 라고 쓰면 매일 밤 자정(12시)에만 실행됩니다.
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void publishScheduledEpisodes() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 승인(APPROVED) 상태이면서, 예약 시간(scheduledAt)이 지금을 지난 회차들을 찾습니다.
        List<Episode> targetEpisodes = episodeRepository.findByStatusAndScheduledAtBefore(Status.APPROVED, now);

        // 2. 찾은 회차들이 없다면 그냥 조용히 넘어갑니다.
        if (targetEpisodes.isEmpty()) {
            return;
        }

        // 3. 찾은 회차들의 상태를 모두 PUBLISHED(공개)로 바꿉니다.
        for (Episode episode : targetEpisodes) {
            episode.setStatus(Status.PUBLISHED);
            log.info("예약된 회차가 자동 업로드되었습니다. 회차 ID: {}, 작품명: {}",
                    episode.getEpisodeId(), episode.getContent().getTitle());
        }

        // @Transactional이 붙어있으므로 (더티 체킹) 별도로 save()를 호출하지 않아도 DB에 자동 반영됩니다!
    }
}
