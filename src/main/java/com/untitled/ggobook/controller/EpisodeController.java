package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.enums.Status;
import com.untitled.ggobook.service.ContentService;
import com.untitled.ggobook.service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @RequestPart("file") MultipartFile multipartFile
    ){
        Content content = contentService.getContentByContentID(contentId);
        episodeService.registerEpisode(content, episode, multipartFile);

        return ResponseEntity.ok("회차 업로드 성공");
    }

    @PatchMapping("/episodes/{episodeId}")
    public void updateEpisode(
            @PathVariable Long episodeId,
            @RequestParam("episode") Episode episode,
            @RequestPart("file") MultipartFile multipartFile){
        episode.setEpisodeId(episodeId);
        episodeService.updateEpisode(episode, multipartFile);
    }

    @DeleteMapping("/episodes/{episodeId}")
    public void deleteEpisode(@PathVariable Long episodeId){
        episodeService.deleteEpisode(episodeId);
    }


}
