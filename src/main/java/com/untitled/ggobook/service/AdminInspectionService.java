package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Notification;
import com.untitled.ggobook.domain.enums.Status;
import com.untitled.ggobook.repository.ContentRepository;
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
    private final ContentRepository contentRepository;
    private final AIRequestUtil aiRequestUtil;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<Content> getInspectionList() {
        return contentRepository.findByStatusInWithAuthor(List.of(Status.PENDING, Status.DRAFT));
    }

    @Transactional(readOnly = true)
    public Content getContentDetail(Long contentId) {
        return contentRepository.findByIdWithAuthor(contentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작품을 찾을 수 없습니다."));
    }

    @Transactional
    public void approveContent(Long contentId) {
        Content content = getContentDetail(contentId);
        content.approve();
        notificationService.send(
                content.getAuthor().getId(),
                String.format("[%s] 작품이 승인되었습니다. 회차를 등록하고 연재를 시작하세요!", content.getTitle()),
                Notification.NotificationType.APPROVE,
                "/author/contents"
        );
    }

    @Transactional
    public void rejectContent(Long contentId, String rejectReason) {
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new IllegalArgumentException("반려 사유는 필수 입력 사항입니다.");
        }
        Content content = getContentDetail(contentId);
        content.reject(rejectReason);
        notificationService.send(
                content.getAuthor().getId(),
                String.format("[%s] 작품이 반려되었습니다. [사유: %s]", content.getTitle(), rejectReason),
                Notification.NotificationType.REJECT,
                "/author/contents"
        );
    }

    @Transactional(readOnly = true)
    public Episode getEpisodeDetail(Long episodeId) {
        return episodeRepository.findByIdWithDetails(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회차를 찾을 수 없습니다."));
    }

    @Transactional
    public void approveEpisode(Long episodeId, LocalDateTime scheduledAt) {

        Episode episode = getEpisodeDetail(episodeId);
        Content content = episode.getContent();

        String textForAI = episode.getExtractableTextForAI();

        String aiSummary = null;

        if (textForAI != null && !textForAI.trim().isEmpty()) {
            try {
                aiSummary = aiRequestUtil.sendRequest(textForAI);
            } catch (Exception e) {
                e.printStackTrace(); // 어떤 에러인지 상세히 출력
            }
        } else {
            System.out.println("DEBUG: [알림] 추출할 텍스트가 없어 요약을 건너뜁니다.");
        }

        episode.approve(scheduledAt, aiSummary);

        if (episode.getEpisodeNumber() == 1 && content.getStatus() == Status.PENDING) {
            content.approve();
        }

        String approveMessage = String.format("[%s] %d화가 승인되었습니다. 연재를 시작합니다!",
                content.getTitle(), episode.getEpisodeNumber());

        notificationService.send(
                content.getAuthor().getId(),
                approveMessage,
                Notification.NotificationType.APPROVE,
                "/author/contents"
        );
    }

    @Transactional
    public void rejectEpisode(Long episodeId, String rejectReason) {
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new IllegalArgumentException("반려 사유는 필수 입력 사항입니다.");
        }

        Episode episode = getEpisodeDetail(episodeId);
        Content content = episode.getContent();

        episode.reject(rejectReason);

        if (episode.getEpisodeNumber() == 1 && content.getStatus() == Status.PENDING) {
            content.reject("1화 반려로 인한 기각: " + rejectReason);
        }

        // 🌟 반려 알림 발송 (경로 통일 및 사유 포함)
        String rejectMessage = String.format("[%s] %d화가 반려되었습니다. [사유: %s]",
                content.getTitle(),
                episode.getEpisodeNumber(),
                rejectReason);

        notificationService.send(
                content.getAuthor().getId(),
                rejectMessage,
                Notification.NotificationType.REJECT,
                "/author/contents"
        );
    }

    @Transactional(readOnly = true)
    public List<Episode> getApprovedList() {
        return episodeRepository.findByStatus(Status.APPROVED);
    }

    @Transactional
    public void blindContent(Long episodeId, String reason) {
        Episode episode = getEpisodeDetail(episodeId);
        episode.blind(reason);

        // 🌟 블라인드 알림 발송 (경로 통일)
        String blindMessage = String.format("[%s] %d화가 관리자에 의해 블라인드 처리되었습니다. [사유: %s]",
                episode.getContent().getTitle(),
                episode.getEpisodeNumber(),
                reason);

        notificationService.send(
                episode.getContent().getAuthor().getId(),
                blindMessage,
                Notification.NotificationType.REJECT,
                "/author/contents"
        );
    }
}