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

    // 청크 정보 조회 (totalChunks + 기존 생성된 청크 URL)
    @GetMapping("/episodes/{episodeId}/tts/chunk-info")
    public ResponseEntity<java.util.Map<String, Object>> getChunkInfo(
            @PathVariable Long episodeId,
            @RequestParam Long voiceId
    ) {
        return ResponseEntity.ok(ttsService.getChunkInfo(episodeId, voiceId));
    }

    // 특정 청크 생성 (캐시 있으면 재사용)
    @PostMapping("/episodes/{episodeId}/tts/chunk/{chunkIndex}")
    public ResponseEntity<java.util.Map<String, Object>> generateChunk(
            @PathVariable Long episodeId,
            @PathVariable Integer chunkIndex,
            @RequestParam Long voiceId
    ) {
        String url = ttsService.generateTtsChunk(episodeId, voiceId, chunkIndex);
        return ResponseEntity.ok(java.util.Map.of("url", url, "chunkIndex", chunkIndex));
    }

    // 멀티보이스 청크 정보 조회
    @GetMapping("/episodes/{episodeId}/tts/multi-voice/chunk-info")
    public ResponseEntity<java.util.Map<String, Object>> getMultiVoiceChunkInfo(
            @PathVariable Long episodeId,
            @RequestParam Long voice1Id,
            @RequestParam Long voice2Id,
            @RequestParam Long narratorVoiceId
    ) {
        return ResponseEntity.ok(ttsService.getMultiVoiceChunkInfo(episodeId, voice1Id, voice2Id, narratorVoiceId));
    }

    // 멀티보이스 특정 청크 생성 (캐시 있으면 재사용)
    @PostMapping("/episodes/{episodeId}/tts/multi-voice/chunk/{segmentIndex}")
    public ResponseEntity<java.util.Map<String, Object>> generateMultiVoiceChunk(
            @PathVariable Long episodeId,
            @PathVariable Integer segmentIndex,
            @RequestParam Long voice1Id,
            @RequestParam Long voice2Id,
            @RequestParam Long narratorVoiceId
    ) {
        String url = ttsService.generateMultiVoiceChunk(episodeId, segmentIndex, voice1Id, voice2Id, narratorVoiceId);
        return ResponseEntity.ok(java.util.Map.of("url", url, "chunkIndex", segmentIndex));
    }

    // 목소리 미리듣기 ("안녕하세요 꼬북입니다" 고정 텍스트)
    @PostMapping("/tts/preview")
    public ResponseEntity<java.util.Map<String, Object>> previewVoice(@RequestParam Long voiceId) {
        String url = ttsService.generatePreview(voiceId);
        return ResponseEntity.ok(java.util.Map.of("url", url));
    }

    // 사용 가능한 목소리 목록 조회
    @GetMapping("/tts/voices")
    public ResponseEntity<List<TtsVoice>> getVoices() {
        return ResponseEntity.ok(ttsService.getVoices());
    }

    // [관리자] 목소리 추가
    @PostMapping("/admin/tts/voices")
    public ResponseEntity<TtsVoice> addVoice(@RequestBody TtsVoice voice) {
        return ResponseEntity.ok(ttsService.addVoice(voice));
    }

    // [관리자] 목소리 수정 (남성/여성 전환 등)
    @PutMapping("/admin/tts/voices/{voiceId}")
    public ResponseEntity<TtsVoice> updateVoice(
            @PathVariable Long voiceId,
            @RequestBody TtsVoice voice
    ) {
        return ResponseEntity.ok(ttsService.updateVoice(voiceId, voice));
    }

    // [관리자] 목소리 삭제
    @DeleteMapping("/admin/tts/voices/{voiceId}")
    public ResponseEntity<Void> deleteVoice(@PathVariable Long voiceId) {
        ttsService.deleteVoice(voiceId);
        return ResponseEntity.ok().build();
    }

    // [관리자] 특정 작품의 전체 에피소드 TTS 일괄 생성
    @PostMapping("/admin/tts/contents/{contentId}")
    public ResponseEntity<String> batchGenerateTts(
            @PathVariable Long contentId,
            @RequestParam Long voiceId
    ) {
        ttsService.batchGenerateTts(contentId, voiceId);
        return ResponseEntity.ok("TTS 일괄 생성 완료");
    }

    // ==================== 릴레이소설 TTS ====================

    // 릴레이소설 단일 보이스 청크 정보 조회
    @GetMapping("/relay-novels/{relayNovelId}/tts/chunk-info")
    public ResponseEntity<java.util.Map<String, Object>> getRelayNovelChunkInfo(
            @PathVariable Long relayNovelId,
            @RequestParam Long voiceId
    ) {
        return ResponseEntity.ok(ttsService.getRelayNovelChunkInfo(relayNovelId, voiceId));
    }

    // 릴레이소설 단일 보이스 특정 청크 생성
    @PostMapping("/relay-novels/{relayNovelId}/tts/chunk/{chunkIndex}")
    public ResponseEntity<java.util.Map<String, Object>> generateRelayNovelChunk(
            @PathVariable Long relayNovelId,
            @PathVariable Integer chunkIndex,
            @RequestParam Long voiceId
    ) {
        String url = ttsService.generateRelayNovelTtsChunk(relayNovelId, voiceId, chunkIndex);
        return ResponseEntity.ok(java.util.Map.of("url", url, "chunkIndex", chunkIndex));
    }

    // 릴레이소설 멀티 보이스 청크 정보 조회
    @GetMapping("/relay-novels/{relayNovelId}/tts/multi-voice/chunk-info")
    public ResponseEntity<java.util.Map<String, Object>> getRelayNovelMultiVoiceChunkInfo(
            @PathVariable Long relayNovelId,
            @RequestParam Long voice1Id,
            @RequestParam Long voice2Id,
            @RequestParam Long narratorVoiceId
    ) {
        return ResponseEntity.ok(ttsService.getRelayNovelMultiVoiceChunkInfo(relayNovelId, voice1Id, voice2Id, narratorVoiceId));
    }

    // 릴레이소설 멀티 보이스 특정 청크 생성
    @PostMapping("/relay-novels/{relayNovelId}/tts/multi-voice/chunk/{segmentIndex}")
    public ResponseEntity<java.util.Map<String, Object>> generateRelayNovelMultiVoiceChunk(
            @PathVariable Long relayNovelId,
            @PathVariable Integer segmentIndex,
            @RequestParam Long voice1Id,
            @RequestParam Long voice2Id,
            @RequestParam Long narratorVoiceId
    ) {
        String url = ttsService.generateRelayNovelMultiVoiceChunk(relayNovelId, segmentIndex, voice1Id, voice2Id, narratorVoiceId);
        return ResponseEntity.ok(java.util.Map.of("url", url, "chunkIndex", segmentIndex));
    }
}
