package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.RecentView;
import com.untitled.ggobook.dto.RecentContentDto;
import com.untitled.ggobook.dto.SaveRecentViewRequest;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.EpisodeRepository;
import com.untitled.ggobook.repository.RecentViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecentViewService {

    private final RecentViewRepository recentViewRepository;
    private final ContentRepository contentRepository;
    private final EpisodeRepository episodeRepository;

    @Transactional(readOnly = true)
    public Slice<RecentContentDto> getRecentViewList(Long userId, Pageable pageable) {
        Slice<RecentView> views = recentViewRepository.findByUserIdOrderByViewedAtDesc(userId, pageable);

        return views.map(view -> new RecentContentDto(
                view.getContent().getContentId(),
                view.getContent().getTitle() != null ? view.getContent().getTitle() : "제목 없음",
                (view.getContent().getAuthor() != null && view.getContent().getAuthor().getNickname() != null)
                        ? view.getContent().getAuthor().getNickname() : "알 수 없는 작가",
                view.getContent().getThumbnailUrl(),
                view.getLastEpisode() != null ? view.getLastEpisode().getEpisodeId() : null,
                view.getLastEpisode() != null ? view.getLastEpisode().getEpisodeNumber() + "화" : "1화",
                view.getProgress(),
                view.getViewedAt(),

                // 🌟 빨간 줄 해결: getContentType()이 아니라 getType()으로 정확히 꺼냅니다!
                view.getContent().getType() != null ? view.getContent().getType() : "WEBTOON"
        ));
    }

    @Transactional
    public void saveRecentView(Long userId, SaveRecentViewRequest request) {
        Optional<RecentView> existingView = recentViewRepository.findByUserIdAndContent_ContentId(userId, request.getContentId());

        Episode episode = episodeRepository.findById(request.getEpisodeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회차입니다."));

        if (existingView.isPresent()) {
            existingView.get().updateViewStatus(episode, request.getProgress());
        } else {
            Content content = contentRepository.findById(request.getContentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 작품입니다."));

            RecentView newView = new RecentView(userId, content, episode, request.getProgress());
            recentViewRepository.save(newView);
        }
    }
}