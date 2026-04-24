package com.untitled.ggobook.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SaveRecentViewRequest {
    private Long contentId; // 어떤 작품?
    private Long episodeId; // 몇 화?
    private int progress;   // 몇 퍼센트(%)?
}