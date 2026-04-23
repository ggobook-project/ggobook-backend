package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.LikedContentDto;
import com.untitled.ggobook.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{contentId}")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long contentId,
            @AuthenticationPrincipal Long id
    ) {
        likeService.toggleLike(id, contentId);
        return ResponseEntity.ok("찜 상태가 변경되었습니다.");
    }

    @GetMapping
    public ResponseEntity<Slice<LikedContentDto>> getLikedContentList(
            @AuthenticationPrincipal Long id,
            Pageable pageable) {

        Slice<LikedContentDto> response = likeService.getLikedContentList(id, pageable);
        return ResponseEntity.ok(response);
    }
}