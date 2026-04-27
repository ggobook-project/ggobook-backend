package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Content;
import lombok.Data;

@Data
public class NovelListDTO {
    private Long contentId;
    private String title;
    private String genre;
    private String thumbnailUrl;
    private String type;
    private String status;
    private String authorNickname;

    public NovelListDTO(Content content) {
        this.contentId = content.getContentId();
        this.title = content.getTitle();
        this.genre = content.getGenre() != null ? content.getGenre() : "장르 미상";
        this.thumbnailUrl = content.getThumbnailUrl();
        this.type = content.getType();
        this.status = content.getStatus().name();
        this.authorNickname = (content.getAuthor() != null)
                ? content.getAuthor().getNickname()
                : "작가 미상";
    }
}