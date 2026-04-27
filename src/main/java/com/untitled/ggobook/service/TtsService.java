package com.untitled.ggobook.service;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.untitled.ggobook.domain.Novel;
import com.untitled.ggobook.domain.TtsVoice;
import com.untitled.ggobook.dto.TtsResponseDto;
import com.untitled.ggobook.repository.NovelRepository;
import com.untitled.ggobook.repository.TtsVoiceRepository;
import com.untitled.ggobook.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TtsService {

    private final TextToSpeechClient textToSpeechClient;
    private final NovelRepository novelRepository;
    private final TtsVoiceRepository ttsVoiceRepository;
    private final FileUtil fileUtil;

    // Google TTS 한 번 요청당 최대 허용 글자 수
    private static final int MAX_CHARS_PER_REQUEST = 4500;

    public TtsResponseDto generateTts(Long episodeId, Long voiceId) {
        Novel novel = novelRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("소설 텍스트를 찾을 수 없습니다."));

        if (novel.getContentText() == null || novel.getContentText().isBlank()) {
            throw new IllegalArgumentException("소설 내용이 없습니다.");
        }

        TtsVoice ttsVoice = ttsVoiceRepository.findById(voiceId)
                .orElseThrow(() -> new IllegalArgumentException("목소리를 찾을 수 없습니다."));

        byte[] audioData = synthesize(novel.getContentText(), ttsVoice.getVoiceName());

        if (novel.getTtsFileUrl() != null) {
            fileUtil.deleteFromS3(novel.getTtsFileUrl());
        }

        String ttsFileUrl = fileUtil.uploadAudioToS3(audioData);
        novel.setTtsFileUrl(ttsFileUrl);
        novelRepository.save(novel);

        return new TtsResponseDto(ttsFileUrl, ttsVoice.getVoiceName());
    }

    @Transactional(readOnly = true)
    public String getTtsUrl(Long episodeId) {
        Novel novel = novelRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("소설을 찾을 수 없습니다."));
        if (novel.getTtsFileUrl() == null) {
            throw new IllegalStateException("아직 TTS가 생성되지 않은 에피소드입니다.");
        }
        return novel.getTtsFileUrl();
    }

    @Transactional(readOnly = true)
    public List<TtsVoice> getVoices() {
        return ttsVoiceRepository.findAll();
    }

    // 긴 텍스트를 청크로 나눠 순서대로 합성 후 MP3 바이트 배열로 반환
    private byte[] synthesize(String text, String voiceName) {
        List<String> chunks = splitIntoChunks(text);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("ko-KR")
                .setName(voiceName)
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build();

        for (String chunk : chunks) {
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(chunk)
                    .build();

            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioContent = response.getAudioContent();
            outputStream.writeBytes(audioContent.toByteArray());
        }

        return outputStream.toByteArray();
    }

    // 문장 경계 기준으로 MAX_CHARS_PER_REQUEST 이하의 청크로 분리
    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?\\n])");

        StringBuilder current = new StringBuilder();
        for (String sentence : sentences) {
            if (current.length() + sentence.length() > MAX_CHARS_PER_REQUEST) {
                if (!current.isEmpty()) {
                    chunks.add(current.toString());
                    current = new StringBuilder();
                }
                // 단일 문장이 한계를 초과하면 강제로 자름
                if (sentence.length() > MAX_CHARS_PER_REQUEST) {
                    int start = 0;
                    while (start < sentence.length()) {
                        int end = Math.min(start + MAX_CHARS_PER_REQUEST, sentence.length());
                        chunks.add(sentence.substring(start, end));
                        start = end;
                    }
                    continue;
                }
            }
            current.append(sentence);
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }

        return chunks;
    }
}
