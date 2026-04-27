package com.untitled.ggobook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TtsResponseDto {
    private String ttsFileUrl;
    private String voiceName;
}
