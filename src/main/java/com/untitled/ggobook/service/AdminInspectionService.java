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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminInspectionService {

    private final EpisodeRepository episodeRepository;
    private final ContentRepository contentRepository; // 🌟 추가: 작품 DB 조회용
    private final AIRequestUtil aiRequestUtil;
    private final NotificationService notificationService;

    // 🌟 핵심 수술: 작품과 회차를 하나로 묶어주는 통합 컨베이어 벨트!
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getInspectionMergedList() {
        // 1. 검수 대기 중인 작품 가져오기
        List<Content> contents = contentRepository.findByStatusInWithAuthor(List.of(Status.PENDING));
        // 2. 검수 대기 중인 회차 가져오기
        List<Episode> episodes = episodeRepository.findByStatus(Status.PENDING);

        List<Map<String, Object>> list = new ArrayList<>();

        // 3. 작품 데이터를 공통 규격 박스(Map)에 포장
        for (Content c : contents) {
            Map<String, Object> map = new HashMap<>();
            map.put("inspectionType", "CONTENT"); // 이름표
            map.put("id", c.getContentId());
            map.put("title", c.getTitle());
            map.put("author", c.getAuthor() != null ? c.getAuthor().getNickname() : "미상");
            map.put("type", c.getType());
            map.put("thumbnailUrl", c.getThumbnailUrl());
            map.put("createdAt", c.getCreatedAt());
            list.add(map);
        }

        // 4. 회차 데이터를 공통 규격 박스(Map)에 포장
        for (Episode e : episodes) {
            Map<String, Object> map = new HashMap<>();
            map.put("inspectionType", "EPISODE"); // 이름표
            map.put("id", e.getEpisodeId());
            map.put("title", e.getEpisodeTitle());
            map.put("episodeNumber", e.getEpisodeNumber());
            map.put("author", e.getContent().getAuthor() != null ? e.getContent().getAuthor().getNickname() : "미상");
            map.put("type", e.getContent().getType());
            map.put("thumbnailUrl", e.getThumbnailUrl());
            map.put("createdAt", e.getCreatedAt());
            list.add(map);
        }
        return list;
    }

    // ==========================================
    //  [신설] 작품(Content) 검수 로직
    // ==========================================
    @Transactional(readOnly = true)
    public Map<String, Object> getContentInspectionDetail(Long contentId) {
        Content c = contentRepository.findById(contentId).orElseThrow();
        Map<String, Object> map = new HashMap<>();
        map.put("title", c.getTitle());
        map.put("author", c.getAuthor() != null ? Map.of("nickname", c.getAuthor().getNickname(), "id", c.getAuthor().getId()) : null);
        map.put("genre", c.getGenre());
        map.put("type", c.getType());
        map.put("createdAt", c.getCreatedAt());
        map.put("summary", c.getSummary());
        map.put("description", c.getDescription());
        map.put("thumbnailUrl", c.getThumbnailUrl());
        return map;
    }

    @Transactional
    public void approveContent(Long contentId) {
        Content c = contentRepository.findById(contentId).orElseThrow();
        c.approve();
        if (c.getAuthor() != null) {
            notificationService.send(c.getAuthor().getId(), "[" + c.getTitle() + "] 작품이 승인되었습니다.", Notification.NotificationType.APPROVE, "/author/contents");
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
    //  [기존 유지] 회차(Episode) 검수 로직
    // ==========================================
    @Transactional(readOnly = true)
    public Episode getEpisodeDetail(Long episodeId) {
        return episodeRepository.findByIdWithDetails(episodeId).orElseThrow();
    }

    @Transactional
    public void approveEpisode(Long episodeId, LocalDateTime scheduledAt) {
        Episode episode = getEpisodeDetail(episodeId);
        Content content = episode.getContent();
        String textForAI = episode.getExtractableTextForAI();
        String aiSummary = null;

        if (textForAI != null && !textForAI.trim().isEmpty()) {
            try { aiSummary = aiRequestUtil.sendRequest(textForAI); } catch (Exception e) { e.printStackTrace(); }
        }

        episode.approve(scheduledAt, aiSummary);

        if (episode.getEpisodeNumber() == 1 && content.getStatus() == Status.PENDING) {
            content.approve();
        }

        if (content.getAuthor() != null) {
            notificationService.send(content.getAuthor().getId(), String.format("[%s] %d화가 승인되었습니다.", content.getTitle(), episode.getEpisodeNumber()), Notification.NotificationType.APPROVE, "/author/contents");
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