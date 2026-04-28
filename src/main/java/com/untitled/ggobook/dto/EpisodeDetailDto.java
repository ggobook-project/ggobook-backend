package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.ComicToon;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EpisodeDetailDto {
    private Long episodeId;
    private Integer episodeNumber;
    private String episodeTitle;
    private Boolean isFree;
    private Integer price;
    private String status;
    private LocalDateTime createdAt;

    // 웹툰용
    private List<ComicToon> comicToons;

    // 웹소설용
    private String contentText;

    // 읽음 여부
    private Boolean isRead;

    // 소장 여부
    private Boolean isOwned;
}

