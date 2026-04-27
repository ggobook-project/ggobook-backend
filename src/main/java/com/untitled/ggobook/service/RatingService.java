package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Rating;
import com.untitled.ggobook.dto.RatingRequestDto;
import com.untitled.ggobook.dto.RatingResponseDto;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ContentRepository contentRepository; // 작품 유무 확인용

    // 1. 별점 등록 및 수정 (Upsert: 없으면 Insert, 있으면 Update)
    @Transactional
    public void upsertRating(Long contentId, Long userId, RatingRequestDto requestDto) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 작품입니다."));

        Optional<Rating> existingRating = ratingRepository.findByContent_ContentIdAndId(contentId, userId);

        if (existingRating.isPresent()) {
            // 이미 별점을 줬다면 점수만 덮어쓰기 (JPA 더티 체킹)
            Rating rating = existingRating.get();
            rating.setScore(requestDto.getScore());
            rating.setUpdatedAt(LocalDateTime.now());
        } else {
            // 처음 주는 별점이라면 새로 생성
            Rating newRating = new Rating();
            newRating.setContent(content);
            newRating.setId(userId); // 팀장님 엔티티의 유저 ID 필드명
            newRating.setScore(requestDto.getScore());
            ratingRepository.save(newRating);
        }
    }

    // 2. 작품 평균 별점 조회
    @Transactional(readOnly = true)
    public double getAverageRating(Long contentId) {
        return Math.round(ratingRepository.getAverageScoreByContentId(contentId) * 10) / 10.0; // 소수점 첫째 자리까지 반올림
    }

    // 3. 내 별점 조회
    @Transactional(readOnly = true)
    public RatingResponseDto getMyRating(Long contentId, Long userId) {
        return ratingRepository.findByContent_ContentIdAndId(contentId, userId)
                .map(rating -> new RatingResponseDto(rating.getScore()))
                .orElse(new RatingResponseDto(0.0)); // 없으면 0점 반환
    }
}