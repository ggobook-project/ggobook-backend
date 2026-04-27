package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.RatingRequestDto;
import com.untitled.ggobook.dto.RatingResponseDto;
import com.untitled.ggobook.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // 1. 평균 별점 조회 (리액트의 loadAverageRating)
    @GetMapping("/{contentId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long contentId) {
        double averageScore = ratingService.getAverageRating(contentId);
        return ResponseEntity.ok(averageScore);
    }

    // 2. 내 별점 조회 (리액트의 loadMyRating)
    @GetMapping("/{contentId}/users/{userId}")
    public ResponseEntity<RatingResponseDto> getMyRating(
            @PathVariable Long contentId,
            @PathVariable Long userId) {
        RatingResponseDto response = ratingService.getMyRating(contentId, userId);
        return ResponseEntity.ok(response);
    }

    // 3. 별점 등록 및 수정 (리액트의 handleRatingConfirm)
    // 주의: 프론트엔드에서 ?userId=xx 형태로 쿼리스트링으로 보내고 있어서 @RequestParam 사용
    @PostMapping("/{contentId}")
    public ResponseEntity<String> saveRating(
            @PathVariable Long contentId,
            @RequestParam Long userId,
            @RequestBody RatingRequestDto requestDto) {
        ratingService.upsertRating(contentId, userId, requestDto);
        return ResponseEntity.ok("별점 저장 성공");
    }
}