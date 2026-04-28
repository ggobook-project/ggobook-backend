package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.OwnedContent;
import com.untitled.ggobook.dto.OwnedEpisodeDto;
import com.untitled.ggobook.service.OwnedContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owns")
@RequiredArgsConstructor
public class OwnedContentController {

    private final OwnedContentService ownedContentService;

    @GetMapping("/")
    public ResponseEntity<List<Content>> getOwnedContents(
            @AuthenticationPrincipal Long id
    ){
        return ResponseEntity.ok(ownedContentService.getOwnedContents(id));
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<List<OwnedEpisodeDto>> getOwnedEpisodesByContent(
            @AuthenticationPrincipal Long id,
            @PathVariable Long contentId
    ){
        return ResponseEntity.ok(ownedContentService.getOwnedEpisodesByContent(id, contentId));
    }

    @DeleteMapping("/{contentId}/{episodeId}")
    public ResponseEntity<String> deleteOwnedContent(
            @AuthenticationPrincipal Long id,
            @PathVariable Long contentId,
            @PathVariable Long episodeId
    ){
        ownedContentService.deleteOwnedContent(id, contentId, episodeId);
        return ResponseEntity.ok("소장 작품 삭제가 완료 되었습니다.");
    }
}
