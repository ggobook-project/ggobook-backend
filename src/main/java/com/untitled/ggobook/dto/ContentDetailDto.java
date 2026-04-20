package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Episode;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Slice;

@Data
@AllArgsConstructor
public class ContentDetailDto {
    private Long contentId;
    private String title;
    private String type;
    private String genre;
    private String summary;
    private String thumbnailUrl;
    private Slice<Episode> episodes;
}