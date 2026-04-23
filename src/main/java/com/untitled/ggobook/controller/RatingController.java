package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Rating;
import com.untitled.ggobook.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 별점 컨트롤러
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/{contentId}")
    public ResponseEntity<String> submitRating(
            @RequestParam("id") Long id,
            @PathVariable Long contentId,
            @RequestBody Rating rating
    ) {

        ratingService.submitRating(id, contentId, rating);

        return ResponseEntity.ok("별점 업로드 성공");

    }

    @GetMapping("/{contentId}")
    public double getAverageRating(@PathVariable Long contentId){

        return ratingService.getAverageRating(contentId);

    }

    @GetMapping("/{contentId}/users/{id}")
    public Rating getRating(
            @PathVariable Long contentId,
            @PathVariable Long id
    ){
        System.out.println("백엔드 들어옴");
        return ratingService.getByIdAndContentId(id, contentId);
    }
}
