package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Rating;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.RatingRepository;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 별점 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ContentRepository contentRepository;


    // ✅ 수정 - 바로 id 사용
    @Transactional
    public void submitRating(Long id, Long contentId, Rating rating) {
        rating.setContent(contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품 없음")));
        rating.setId(id);

        Rating existing = ratingRepository.findByIdAndContent_ContentId(id, contentId);
        if (existing != null) {
            rating.setRatingId(existing.getRatingId());
            rating.setUpdatedAt(LocalDateTime.now());
        }

        ratingRepository.save(rating);
    }
    public double getAverageRating(Long contentId) {
        return ratingRepository.findAverageByContentId(contentId) != null ? ratingRepository.findAverageByContentId(contentId) : 0.0;
    }


    public Rating getByIdAndContentId(Long id, Long contentId) {
        return ratingRepository.findByIdAndContent_ContentId(id, contentId);
    }
}
