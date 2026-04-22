package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Notification;
import com.untitled.ggobook.domain.enums.Status;
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
        String aiSummary = "웹툰은 요약이 제공되지 않습니다."; // 기본값 세팅

        // AI 텍스트가 존재하면 요약 요청
        if (textForAI != null && !textForAI.trim().isEmpty()) {
            try {
                aiSummary = aiRequestUtil.sendRequest(textForAI);
            } catch (Exception e) {
                aiSummary = "AI 요약 생성에 실패했습니다."; // 장애 방어
            }
        }

        // 🌟 [수정] 무분별한 Setter 대신, 명확한 도메인 메서드 호출!
        episode.approve(scheduledAt, aiSummary);

        if (episode.getEpisodeNumber() == 1 && content.getStatus() == Status.PENDING) {
            content.approve(); // 1화면 작품 자체도 승인
        }

        // ✅ [기존 로직 유지] 승인 완료 알림 발송
        String approveMessage = String.format("[%s] %d화가 승인되었습니다. 연재를 시작합니다!",
                content.getTitle(),
                episode.getEpisodeNumber());

        notificationService.send(
                content.getAuthorId(),
                approveMessage,
                Notification.NotificationType.APPROVE,
                "/author/works/" + content.getContentId()
        );
    }

    @Transactional
    public void rejectEpisode(Long episodeId, String rejectReason) {
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new IllegalArgumentException("반려 사유는 필수 입력 사항입니다.");
        }

        Episode episode = getEpisodeDetail(episodeId);
        Content content = episode.getContent();

        // 🌟 [수정] Setter 대신 도메인 메서드 호출
        episode.reject(rejectReason);

        if (episode.getEpisodeNumber() == 1 && content.getStatus() == Status.PENDING) {
            content.reject("1화 반려로 인한 기각: " + rejectReason);
        }

        // 알림 발송 (기존 로직 유지)
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

    // ==========================================
    // 🌟 [신규 추가] 일반 작품/회차 강제 블라인드
    // ==========================================
    @Transactional
    public void blindContent(Long episodeId, String reason) {
        Episode episode = getEpisodeDetail(episodeId);

        // 일반 회차는 릴레이가 아니므로 AI 스토리 브릿지 없이 바로 가림 처리
        episode.blind(reason);

        // (선택) 작가에게 블라인드 당했다고 알림 보내기
        String blindMessage = String.format("[%s] %d화가 관리자에 의해 블라인드 처리되었습니다. 사유: %s",
                episode.getContent().getTitle(),
                episode.getEpisodeNumber(),
                reason);

        notificationService.send(
                episode.getContent().getAuthorId(),
                blindMessage,
                Notification.NotificationType.REJECT, // 알림 타입 조정 필요
                "/author/works/" + episode.getEpisodeId()
        );
    }

}