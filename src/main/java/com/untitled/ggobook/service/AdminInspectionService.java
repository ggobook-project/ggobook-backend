package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Status;
import com.untitled.ggobook.repository.EpisodeRepository;
import com.untitled.ggobook.util.AIRequestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminInspectionService {

    private final EpisodeRepository episodeRepository;
    private final AIRequestUtil aiRequestUtil;

    @Transactional(readOnly = true)
    public List<Episode> getInspectionList() {
        return episodeRepository.findByStatus(Status.PENDING);
    }

    @Transactional
    public void approveEpisode(Episode episode, LocalDateTime scheduledAt) {
        Content content = episode.getContent();

        String textForAI = episode.getExtractableTextForAI();

        if (textForAI != null && !textForAI.trim().isEmpty()) {
            String aiSummary = aiRequestUtil.sendRequest(textForAI);
            episode.setAiSummary(aiSummary);
        } else {
            episode.setAiSummary("웹툰은 요약이 제공되지 않습니다.");
        }

        episode.setStatus(Status.APPROVED);
        episode.setScheduledAt(scheduledAt);

        if (episode.getEpisodeNumber() == 1 && content.getStatus() == Status.PENDING) {
            content.setStatus(Status.APPROVED);
        }
    }

    @Transactional
    public void rejectEpisode(Episode episode, String rejectReason) {
        Content content = episode.getContent();

        episode.setStatus(Status.REJECTED);
        episode.setRejectReason(rejectReason);

        if (episode.getEpisodeNumber() == 1 && content.getStatus() == Status.PENDING) {
            content.setStatus(Status.REJECTED);
            content.setRejectReason("1화 반려로 인한 기각: " + rejectReason);
        }
    }
}