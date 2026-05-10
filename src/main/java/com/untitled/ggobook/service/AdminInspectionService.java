package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Notification;
import com.untitled.ggobook.domain.enums.Status;
import com.untitled.ggobook.dto.ContentBasicDTO;
import com.untitled.ggobook.dto.EpisodeDTO;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.EpisodeRepository;
import com.untitled.ggobook.util.AIRequestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminInspectionService {

    private final EpisodeRepository episodeRepository;
    private final ContentRepository contentRepository;
    private final AIRequestUtil aiRequestUtil;
    private final NotificationService notificationService;

    // ==========================================
    //  🌟 수정 1: Map 통합 리스트 폐기 -> 기존 DTO 분리 반환
    // ==========================================
    @Transactional(readOnly = true)
    public List<ContentBasicDTO> getPendingContents() {
        return contentRepository.findByStatusInWithAuthor(List.of(Status.PENDING))
                .stream()
                .map(ContentBasicDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EpisodeDTO> getPendingEpisodes() {
        return episodeRepository.findByStatus(Status.PENDING)
                .stream()
                .map(EpisodeDTO::new)
                .collect(Collectors.toList());
    }

    // ==========================================
    //  🌟 수정 2: 작품 상세 검수 (Map 폐기 -> 기존 DTO 반환)
    // ==========================================
    @Transactional(readOnly = true)
    public ContentBasicDTO getContentInspectionDetail(Long contentId) {
        Content c = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품을 찾을 수 없습니다."));
        return new ContentBasicDTO(c);
    }

    @Transactional
    public void approveContent(Long contentId) {
        Content c = contentRepository.findById(contentId).orElseThrow();

        // 🌟 핵심 수술: 복제본인지 확인하고 덮어쓰기!
        if (c.getOriginalId() != null) {
            Content original = contentRepository.findById(c.getOriginalId()).orElseThrow();
            original.setTitle(c.getTitle());
            original.setType(c.getType());
            original.setGenre(c.getGenre());
            original.setSummary(c.getSummary());
            original.setDescription(c.getDescription());
            original.setSerialDay(c.getSerialDay());
            original.setVideoUrl(c.getVideoUrl());
            if (c.getThumbnailUrl() != null) {
                original.setThumbnailUrl(c.getThumbnailUrl());
            }

            contentRepository.delete(c); // 덮어씌운 후 복제본은 깔끔하게 삭제!

            if (original.getAuthor() != null) {
                notificationService.send(original.getAuthor().getId(), "[" + original.getTitle() + "] 작품 수정이 승인되었습니다.", Notification.NotificationType.APPROVE, "/author/contents");
            }
        } else {
            // 신규 승인
            c.approve();
            if (c.getAuthor() != null) {
                notificationService.send(c.getAuthor().getId(), "[" + c.getTitle() + "] 작품이 승인되었습니다.", Notification.NotificationType.APPROVE, "/author/contents");
            }
        }
    }

    @Transactional
    public void rejectContent(Long contentId, String reason) {
        Content c = contentRepository.findById(contentId).orElseThrow();
        c.reject(reason);
        if (c.getAuthor() != null) {
            notificationService.send(c.getAuthor().getId(), "[" + c.getTitle() + "] 작품이 반려되었습니다. [사유: " + reason + "]", Notification.NotificationType.REJECT, "/author/contents");
        }
    }

    // ==========================================
    //  회차(Episode) 검수 로직 (엔티티 반환 유지는 컨트롤러에서 DTO 변환)
    // ==========================================
    @Transactional(readOnly = true)
    public Episode getEpisodeDetail(Long episodeId) {
        return episodeRepository.findByIdWithDetails(episodeId).orElseThrow();
    }

    @Transactional
    public void approveEpisode(Long episodeId, LocalDateTime scheduledAt) {
        Episode draft = getEpisodeDetail(episodeId);
        Content content = draft.getContent();

        // 🌟 1. 신규/수정 공통: 먼저 AI 요약을 무조건 진행합니다.
        String textForAI = draft.getExtractableTextForAI();
        String aiSummary = null;

        if (textForAI != null && !textForAI.trim().isEmpty()) {
            try {
                // AI 요약 요청
                aiSummary = aiRequestUtil.sendRequest(textForAI);
            } catch (Exception e) {
                e.printStackTrace();
                // 필요시 log.error("AI 요약 실패", e); 로 변경
            }
        }

        // 🌟 2. 날짜 설정: 작가가 지정한 날짜 최우선
        LocalDateTime finalScheduledAt = draft.getScheduledAt() != null ? draft.getScheduledAt() : scheduledAt;

        // 🌟 3. 분기 처리: 복제본 덮어쓰기 vs 신규 승인
        if (draft.getOriginalId() != null) {
            // [수정본 승인] 기존 원본을 찾아서 덮어씌웁니다.
            Episode original = getEpisodeDetail(draft.getOriginalId());
            original.setEpisodeTitle(draft.getEpisodeTitle());
            original.setIsFree(draft.getIsFree());
            original.setScheduledAt(finalScheduledAt);

            // 💡 추가된 핵심 로직: 새롭게 받아온 AI 요약본도 원본에 덮어씌워 줍니다!
            if (aiSummary != null) {
                original.updateSummary(aiSummary);
            }

            if (draft.getThumbnailUrl() != null) {
                original.setThumbnailUrl(draft.getThumbnailUrl());
            }

            episodeRepository.delete(draft); // 덮어씌운 후 복제본 삭제

            if (content.getAuthor() != null) {
                notificationService.send(content.getAuthor().getId(), String.format("[%s] %d화 수정이 승인되었습니다.", content.getTitle(), original.getEpisodeNumber()), Notification.NotificationType.APPROVE, "/author/contents");
            }
        } else {
            // [신규 승인] 기존 로직 유지하되, 위에서 미리 구한 aiSummary를 전달합니다.
            draft.approve(finalScheduledAt, aiSummary);

            if (draft.getEpisodeNumber() == 1 && content.getStatus() == Status.PENDING) {
                content.approve();
            }

            if (content.getAuthor() != null) {
                notificationService.send(content.getAuthor().getId(), String.format("[%s] %d화가 승인되었습니다.", content.getTitle(), draft.getEpisodeNumber()), Notification.NotificationType.APPROVE, "/author/contents");
            }
        }
    }

    @Transactional
    public void rejectEpisode(Long episodeId, String rejectReason) {
        if (rejectReason == null || rejectReason.trim().isEmpty()) throw new IllegalArgumentException("사유 필수");
        Episode episode = getEpisodeDetail(episodeId);
        Content content = episode.getContent();
        episode.reject(rejectReason);

        if (episode.getEpisodeNumber() == 1 && content.getStatus() == Status.PENDING) {
            content.reject("1화 반려로 인한 기각: " + rejectReason);
        }

        if (content.getAuthor() != null) {
            notificationService.send(content.getAuthor().getId(), String.format("[%s] %d화가 반려되었습니다. [사유: %s]", content.getTitle(), episode.getEpisodeNumber(), rejectReason), Notification.NotificationType.REJECT, "/author/contents");
        }
    }

    @Transactional(readOnly = true)
    public List<Episode> getApprovedList() { return episodeRepository.findByStatus(Status.APPROVED); }

    @Transactional
    public void blindContent(Long episodeId, String reason) {
        Episode episode = getEpisodeDetail(episodeId);
        episode.blind(reason);
        if (episode.getContent().getAuthor() != null) {
            notificationService.send(episode.getContent().getAuthor().getId(), String.format("[%s] %d화가 강제 블라인드 되었습니다. [사유: %s]", episode.getContent().getTitle(), episode.getEpisodeNumber(), reason), Notification.NotificationType.REJECT, "/author/contents");
        }
    }
}