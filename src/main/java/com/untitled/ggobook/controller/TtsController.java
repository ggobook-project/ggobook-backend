package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.TtsVoice;
import com.untitled.ggobook.dto.TtsResponseDto;
import com.untitled.ggobook.service.TtsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TtsController {

    private final TtsService ttsService;

    // 에피소드 TTS 생성 (단일 목소리)
    @PostMapping("/episodes/{episodeId}/tts")
    public ResponseEntity<TtsResponseDto> generateTts(
            @PathVariable Long episodeId,
            @RequestParam Long voiceId
    ) {
        return ResponseEntity.ok(ttsService.generateTts(episodeId, voiceId));
    }

    // 생성된 TTS 오디오 URL 조회
    @GetMapping("/episodes/{episodeId}/tts")
    public ResponseEntity<String> getTtsUrl(@PathVariable Long episodeId) {
        return ResponseEntity.ok(ttsService.getTtsUrl(episodeId));
    }

    // 사용 가능한 목소리 목록 조회
    @GetMapping("/tts/voices")
    public ResponseEntity<List<TtsVoice>> getVoices() {
        return ResponseEntity.ok(ttsService.getVoices());
    }
}
