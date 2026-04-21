package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Likes;
import com.untitled.ggobook.service.ContentService;
import com.untitled.ggobook.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 찜 컨트롤러
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{contentId}")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long contentId,
            @RequestParam("userId") Long userId
    ) {
        likeService.toggleLike(userId, contentId);

        return ResponseEntity.ok("찜 성공");
    }

    @GetMapping("/")
    public Slice<Likes> getLikedContentList(@RequestParam("userId") Long userId, Pageable pageable) {
        return likeService.getLikedContentList(userId, pageable);
    }

}
