package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Rating;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 별점 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ContentRepository contentRepository;

    public void submitRating(Long userId, Long contentId, Rating rating) {
        rating.setContent(contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품 없음")));
        rating.setUserId(userId);

        Rating existing = ratingRepository.findByUserIdAndContent_ContentId(userId, contentId);
        if (existing != null) {
            rating.setRatingId(existing.getRatingId());
        }

        ratingRepository.save(rating);
    }

    public double getAverageRating(Long contentId) {
        return ratingRepository.findAverageByContentId(contentId);
    }


    public Rating getByUserIdAndContentId(Long contentId, Long userId) {
        return ratingRepository.findByUserIdAndContent_ContentId(userId, contentId);
    }
}
