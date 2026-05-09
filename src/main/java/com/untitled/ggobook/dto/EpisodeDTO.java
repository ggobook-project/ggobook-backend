package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Episode;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class EpisodeDTO {
    private Long episodeId;      // 회차 식별 ID
    private Integer episodeNumber; // 몇 화인지 (1, 2, 3...)
    private String title;        // 회차 제목
    private String contentType; // "WEBTOON" 또는 "NOVEL"
    private List<String> imageUrls; // 웹툰용
    private String novelContent;
    private String status;       // 현재 상태 (PUBLISHED, BLINDED 등)
    private LocalDateTime createdAt; // 작성일

    private String authorNickname;
    private String thumbnailUrl;

    public EpisodeDTO() {
        this.imageUrls = new ArrayList<>();
    }

    public EpisodeDTO(Episode episode) {
        this.episodeId = episode.getEpisodeId();
        this.episodeNumber = episode.getEpisodeNumber();
        this.title = episode.getEpisodeTitle();
        this.status = episode.getStatus().name();
        this.createdAt = episode.getCreatedAt();

        if (episode.getContent() != null && episode.getContent().getAuthor() != null) {
            this.authorNickname = episode.getContent().getAuthor().getNickname();
        } else {
            this.authorNickname = "미상";
        }
        this.thumbnailUrl = episode.getThumbnailUrl();
    }
}