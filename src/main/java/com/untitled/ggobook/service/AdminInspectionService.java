package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Notification;
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
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<Episode> getInspectionList() {
        return episodeRepository.findByStatus(Status.PENDING);
    }

    @Transactional(readOnly = true)
    public Episode getEpisodeDetail(Long episodeId) {
        // ✅ [수정] 기본 findById 대신 Fetch Join이 적용된 쿼리를 사용합니다.
        return episodeRepository.findByIdWithDetails(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차를 찾을 수 없습니다."));
    }

    @Transactional
    public void approveEpisode(Long episodeId, LocalDateTime scheduledAt) {
        Episode episode = getEpisodeDetail(episodeId);
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

        // ✅ [추가 로직] 승인 완료 알림 발송
        String approveMessage = String.format("[%s] %d화가 승인되었습니다. 연재를 시작합니다!",
                content.getTitle(),
                episode.getEpisodeNumber());

        notificationService.send(
                content.getAuthorId(),                          // 수신자 (작가 ID)
                approveMessage,                                 // 메시지 내용
                Notification.NotificationType.APPROVE,          // 알림 타입 (승인)
                "/author/works/" + content.getContentId()       // 🎯 클릭 시 이동할 URL (작품 홈)
        );
    }

    @Transactional
    public void rejectEpisode(Long episodeId, String rejectReason) {
        // 🛡️ [안전장치] 반려 사유가 비어있으면 에러를 던져서 데이터베이스 오염을 막습니다.
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new IllegalArgumentException("반려 사유는 필수 입력 사항입니다.");
        }

        Episode episode = getEpisodeDetail(episodeId);
        Content content = episode.getContent();

        // 1. [기존 로직] 회차 및 작품 상태를 반려(REJECTED)로 업데이트
        episode.setStatus(Status.REJECTED);
        episode.setRejectReason(rejectReason);

        if (episode.getEpisodeNumber() == 1 && content.getStatus() == Status.PENDING) {
            content.setStatus(Status.REJECTED);
            content.setRejectReason("1화 반려로 인한 기각: " + rejectReason);
        }

        // 2. [추가 로직] 반려 사유를 포함하여 작가에게 알림 발송
        String rejectMessage = String.format("[%s] %d화가 반려되었습니다. 사유: %s",
                content.getTitle(),
                episode.getEpisodeNumber(),
                rejectReason);

        notificationService.send(
                content.getAuthorId(),
                rejectMessage,
                Notification.NotificationType.REJECT,
                "/author/works/" + episode.getEpisodeId() + "/edit"
        );
    }

    @Transactional(readOnly = true)
    public List<Episode> getApprovedList() {
        return episodeRepository.findByStatus(Status.APPROVED);
    }
}