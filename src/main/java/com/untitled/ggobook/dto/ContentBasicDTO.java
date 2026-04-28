package com.untitled.ggobook.dto;

import com.untitled.ggobook.domain.Content;
import lombok.Data;

@Data
public class ContentBasicDTO {
    private Long contentId;
    private String title;
    private String authorNickname;

    public ContentBasicDTO(Content content) {
        this.contentId = content.getContentId();
        this.title = content.getTitle();
        this.authorNickname = content.getAuthor().getNickname();
    }
}
