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

    @GetMapping("/{episodeId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long episodeId) {
        double averageScore = ratingService.getAverageRating(episodeId);
        return ResponseEntity.ok(averageScore);
    }

    @GetMapping("/{episodeId}/users/{userId}")
    public ResponseEntity<RatingResponseDto> getMyRating(
            @PathVariable Long episodeId,
            @PathVariable Long userId) {
        RatingResponseDto response = ratingService.getMyRating(episodeId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{episodeId}")
    public ResponseEntity<String> saveRating(
            @PathVariable Long episodeId,
            @RequestParam Long userId,
            @RequestBody RatingRequestDto requestDto) {
        ratingService.upsertRating(episodeId, userId, requestDto);
        return ResponseEntity.ok("별점 저장 성공");
    }
}