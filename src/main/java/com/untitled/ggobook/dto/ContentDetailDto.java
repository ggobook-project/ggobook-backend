package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.ContentTag;
import com.untitled.ggobook.domain.Episode;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Slice;

import java.util.List;

@Data
@AllArgsConstructor
public class ContentDetailDto {
    private Long contentId;
    private String title;
    private String type;
    private String genre;
    private String summary;
    private String thumbnailUrl;
    private Slice<EpisodeDetailDto> episodes;

    //이 유저가 이 작품을 찜했는지 여부 추가
    private boolean isLiked;

    private List<ContentTag> tags;

    private String description;
    private String serialDay;
    private String videoUrl;

}