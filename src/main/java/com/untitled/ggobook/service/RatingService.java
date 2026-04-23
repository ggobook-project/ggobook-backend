package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Rating;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.RatingRepository;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// 별점 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    public void submitRating(String userLoginId, Long contentId, Rating rating) {
        User user = userRepository.findByUserId(userLoginId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        rating.setContent(contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품 없음")));
        rating.setUserId(user.getId());

        Rating existing = ratingRepository.findByUserIdAndContent_ContentId(user.getId(), contentId);
        if (existing != null) {
            rating.setRatingId(existing.getRatingId());
            rating.setUpdatedAt(LocalDateTime.now());
        }

        ratingRepository.save(rating);
    }

    public double getAverageRating(Long contentId) {
        return ratingRepository.findAverageByContentId(contentId) != null ? ratingRepository.findAverageByContentId(contentId) : 0.0;
    }


    public Rating getByUserIdAndContentId(Long contentId, Long userId) {
        return ratingRepository.findByUserIdAndContent_ContentId(userId, contentId);
    }
}
