package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.LikedContentDto; //  DTO 추가
import com.untitled.ggobook.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; //  추가
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{contentId}")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long contentId,
            @AuthenticationPrincipal String userId //  프론트에서 RequestParam으로 받지 않고 토큰에서 빼옴
    ) {
        likeService.toggleLike(userId, contentId);
        return ResponseEntity.ok("찜 상태가 변경되었습니다.");
    }

    // @GetMapping("/") 보다는 깔끔하게 @GetMapping 추천
    @GetMapping
    public ResponseEntity<Slice<LikedContentDto>> getLikedContentList( //  응답 타입 DTO로 변경
                                                                       @AuthenticationPrincipal String userId, // 🌟 토큰 적용
                                                                       Pageable pageable) {

        Slice<LikedContentDto> response = likeService.getLikedContentList(userId, pageable);
        return ResponseEntity.ok(response);
    }
}