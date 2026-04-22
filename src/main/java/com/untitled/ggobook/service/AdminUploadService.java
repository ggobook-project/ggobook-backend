package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.enums.Status;
import com.untitled.ggobook.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUploadService {
    private final EpisodeRepository episodeRepository;

    @Transactional(readOnly = true)
    public List<Episode> loadUploadList() {
        // 명세서 메서드명: loadUploadList() 반영
        return episodeRepository.findByStatus(Status.APPROVED);
    }

    @Transactional
    public void toggleVisibility(Long episodeId) {
        // 명세서 메서드명: handleToggleVisibility() 반영
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("회차 없음"));
        episode.setStatus(episode.getStatus() == Status.APPROVED ? Status.BLINDED : Status.APPROVED);
    }

    @Transactional
    public void handleScheduleSubmit(Long episodeId, LocalDateTime scheduledAt) {
        // 명세서 메서드명: handleScheduleSubmit() 반영
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("회차 없음"));
        if (episode.getScheduledAt() != null) throw new IllegalStateException("수정 불가");
        episode.setScheduledAt(scheduledAt);
    }
}