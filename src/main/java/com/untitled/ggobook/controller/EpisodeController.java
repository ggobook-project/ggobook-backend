package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.ComicToon;
import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Novel;
import com.untitled.ggobook.domain.enums.Status;
import com.untitled.ggobook.service.ContentService;
import com.untitled.ggobook.service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// 회차 컨트롤러
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeService episodeService;
    private final ContentService contentService;

    @GetMapping("/contents/{contentId}/episodes")
    public Slice<Episode> getEpisodeList(
            @RequestParam(required = false)Pageable pageable,
            @PathVariable Long contentId,
            String currentNeedStatus
            ){
        return episodeService.getEpisodeList(contentId, pageable, currentNeedStatus);
    }

    @GetMapping("/episodes/{episodeId}")
    public Episode getEpisodeDetail(@PathVariable Long episodeId){
        return episodeService.getEpisodeDetail(episodeId);
    }

    @PostMapping("/contents/{contentId}/episodes")
    public ResponseEntity<String> registerEpisode(
            @PathVariable Long contentId,
            @RequestPart("episode") Episode episode,
            @RequestPart(value = "novel", required = false) Novel novel,
            @RequestPart(value = "thumbFile", required = false) MultipartFile thumbFile,
            @RequestPart(value = "episodeFiles", required = false) List<MultipartFile> episodeFiles
    ){
        Content content = contentService.getContentByContentID(contentId);
        episodeService.registerEpisode(content, episode, novel, thumbFile, episodeFiles);

        return ResponseEntity.ok("회차 업로드 성공");
    }

    @PatchMapping("/episodes/{episodeId}")
    public void updateEpisode(
            @PathVariable Long episodeId,
            @RequestPart("episode") Episode episode,
            @RequestPart(value = "novel", required = false) Novel novel,
            @RequestPart(value = "thumbFile", required = false) MultipartFile thumbFile,
            @RequestPart(value = "episodeFiles", required = false) List<MultipartFile> episodeFiles) {
        episode.setEpisodeId(episodeId);
        episodeService.updateEpisode(episode, novel, thumbFile, episodeFiles);
    }

    @DeleteMapping("/episodes/{episodeId}")
    public void deleteEpisode(@PathVariable Long episodeId){
        episodeService.deleteEpisode(episodeId);
    }


    @PostMapping("/episodes/{episodeId}/purchase")
    public ResponseEntity<String> purchaseEpisode(
            @AuthenticationPrincipal Long id,
            @PathVariable Long episodeId
    ) {
        episodeService.purchaseEpisode(id, episodeId);
        return ResponseEntity.ok("구매 완료");
    }
    // 🌟 추가: 회차 좋아요 토글 API
    @PostMapping("/episodes/{episodeId}/likes")
    public ResponseEntity<String> toggleEpisodeLike(
            @AuthenticationPrincipal Long id,
            @PathVariable Long episodeId) {
        episodeService.toggleEpisodeLike(id, episodeId);
        return ResponseEntity.ok("회차 좋아요 처리 완료");
    }
    @GetMapping("/episodes/{episodeId}/is-liked")
    public ResponseEntity<Boolean> checkEpisodeLike(
            @AuthenticationPrincipal Long id,
            @PathVariable Long episodeId) {
        // 비회원이면 무조건 false 반환
        if (id == null) return ResponseEntity.ok(false);
        return ResponseEntity.ok(episodeService.checkEpisodeLike(id, episodeId));
    }
}
