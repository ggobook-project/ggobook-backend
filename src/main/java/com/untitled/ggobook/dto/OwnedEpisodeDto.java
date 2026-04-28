package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.OwnedContent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OwnedEpisodeDto {
    private Long ownedId;
    private Long episodeId;
    private Integer episodeNumber;
    private String episodeTitle;
    private String thumbnailUrl;
    private LocalDateTime ownedAt;

    public static OwnedEpisodeDto from(OwnedContent ownedContent) {
        return OwnedEpisodeDto.builder()
                .ownedId(ownedContent.getOwnedId())
                .episodeId(ownedContent.getEpisode().getEpisodeId())
                .episodeNumber(ownedContent.getEpisode().getEpisodeNumber())
                .episodeTitle(ownedContent.getEpisode().getEpisodeTitle())
                .thumbnailUrl(ownedContent.getEpisode().getThumbnailUrl())
                .ownedAt(ownedContent.getCreatedAt())
                .build();
    }
}
