package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.ContentBasicDTO;
import com.untitled.ggobook.dto.EpisodeDTO;
import com.untitled.ggobook.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminContentController {

    private final AdminContentService adminContentService;

    @GetMapping("/contents")
    public ResponseEntity<?> getContentsByType(
            @RequestParam String type,
            @RequestParam(required = false) String keyword) {

        // 🌟 서비스 로직 하나로 모든 타입과 검색어 처리
        return ResponseEntity.ok(adminContentService.getContentsByType(type, keyword));
    }
    /**
     * 🌟 특정 작품의 회차 목록을 가져오는 API
     * GET /api/admin/content/{contentId}/episodes
     */
    @GetMapping("/content/{contentId}/episodes")
    public ResponseEntity<List<EpisodeDTO>> getEpisodesByContent(@PathVariable Long contentId) {
        // 서비스에서 DTO 리스트를 받아와 반환합니다.
        List<EpisodeDTO> episodes = adminContentService.getEpisodeList(contentId);
        return ResponseEntity.ok(episodes);
    }

    @GetMapping("/content/{contentId}")
    public ResponseEntity<ContentBasicDTO> getContentInfo(@PathVariable Long contentId) {
        return ResponseEntity.ok(adminContentService.getContentBasicInfo(contentId));
    }

    @PutMapping("/content/episodes/{episodeId}/blind")
    public ResponseEntity<Void> toggleEpisodeBlindStatus(@PathVariable Long episodeId) {
        adminContentService.toggleEpisodeBlindStatus(episodeId);
        return ResponseEntity.ok().build();
    }

}
