package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Rating;
import com.untitled.ggobook.dto.RatingRequestDto;
import com.untitled.ggobook.dto.RatingResponseDto;
import com.untitled.ggobook.repository.EpisodeRepository;
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
    private final EpisodeRepository episodeRepository;

    @Transactional
    public void upsertRating(Long episodeId, Long userId, RatingRequestDto requestDto) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회차입니다."));

        Optional<Rating> existingRating = ratingRepository.findByEpisode_EpisodeIdAndUserId(episodeId, userId);

        if (existingRating.isPresent()) {
            Rating rating = existingRating.get();
            rating.setScore(requestDto.getScore());
            rating.setUpdatedAt(LocalDateTime.now());
        } else {
            Rating newRating = new Rating();
            newRating.setEpisode(episode);
            newRating.setUserId(userId);
            newRating.setScore(requestDto.getScore());
            ratingRepository.save(newRating);
        }
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Long episodeId) {
        return Math.round(ratingRepository.getAverageScoreByEpisodeId(episodeId) * 10) / 10.0;
    }

    @Transactional(readOnly = true)
    public RatingResponseDto getMyRating(Long episodeId, Long userId) {
        return ratingRepository.findByEpisode_EpisodeIdAndUserId(episodeId, userId)
                .map(rating -> new RatingResponseDto(rating.getScore()))
                .orElse(new RatingResponseDto(0.0));
    }
}