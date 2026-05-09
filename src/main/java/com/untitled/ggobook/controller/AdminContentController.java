package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.ContentBasicDTO;
import com.untitled.ggobook.dto.EpisodeDTO;
import com.untitled.ggobook.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // 🌟 추가
import org.springframework.data.domain.Pageable; // 🌟 추가
import org.springframework.data.domain.Sort; // 🌟 추가
import org.springframework.data.web.PageableDefault; // 🌟 추가
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminContentController {

    private final AdminContentService adminContentService;

    // 🌟 수정: Pageable 파라미터 추가 및 기본값(최신순, 10개씩) 설정
    @GetMapping("/contents")
    public ResponseEntity<Page<?>> getContentsByType(
            @RequestParam String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String day,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){

        // 서비스에 pageable 객체를 함께 넘깁니다.
        return ResponseEntity.ok(adminContentService.getContentsByType(type, keyword, day, pageable));
    }

    @GetMapping("/content/{contentId}/episodes")
    public ResponseEntity<List<EpisodeDTO>> getEpisodesByContent(@PathVariable Long contentId) {
        List<EpisodeDTO> episodes = adminContentService.getEpisodeList(contentId);
        return ResponseEntity.ok(episodes);
    }

    @GetMapping("/content/{contentId}")
    public ResponseEntity<ContentBasicDTO> getContentInfo(@PathVariable Long contentId) {
        return ResponseEntity.ok(adminContentService.getContentBasicInfo(contentId));
    }

    @GetMapping("/content/episodes/{episodeId}/view")
    public ResponseEntity<EpisodeDTO> getEpisodeView(@PathVariable Long episodeId) {
        return ResponseEntity.ok(adminContentService.getEpisodeView(episodeId));
    }

    @PutMapping("/content/episodes/{episodeId}/blind")
    public ResponseEntity<Void> toggleEpisodeBlindStatus(@PathVariable Long episodeId) {
        adminContentService.toggleEpisodeBlindStatus(episodeId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/content/{contentId}/blind-all")
    public ResponseEntity<Void> blindEntireContent(@PathVariable Long contentId) {
        adminContentService.blindEntireContent(contentId);
        return ResponseEntity.ok().build();
    }
}