package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Content;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContentBasicDTO {
    private Long contentId;
    private String title;
    private String authorNickname;
    private String type;
    private String genre;
    private String summary;
    private String description;
    private String thumbnailUrl;
    private LocalDateTime createdAt;

    public ContentBasicDTO(Content content) {
        this.contentId = content.getContentId();
        this.title = content.getTitle();
        this.authorNickname = content.getAuthor() != null ? content.getAuthor().getNickname() : "미상";

        // 🌟 추가된 매핑
        this.type = content.getType();
        this.genre = content.getGenre();
        this.summary = content.getSummary();
        this.description = content.getDescription();
        this.thumbnailUrl = content.getThumbnailUrl();
        this.createdAt = content.getCreatedAt();
    }
}