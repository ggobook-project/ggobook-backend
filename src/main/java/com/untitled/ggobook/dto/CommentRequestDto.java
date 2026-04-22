package com.untitled.ggobook.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto {
    private String commentText;
    private Boolean isSpoiler; // 스포일러 체크 여부
}