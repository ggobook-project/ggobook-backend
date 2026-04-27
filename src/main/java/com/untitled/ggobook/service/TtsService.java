package com.untitled.ggobook.service;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Novel;
import com.untitled.ggobook.domain.TtsVoice;
import com.untitled.ggobook.dto.TtsResponseDto;
import com.untitled.ggobook.repository.EpisodeRepository;
import com.untitled.ggobook.repository.NovelRepository;
import com.untitled.ggobook.repository.TtsVoiceRepository;
import com.untitled.ggobook.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TtsService {

    private final TextToSpeechClient textToSpeechClient;
    private final EpisodeRepository episodeRepository;
    private final NovelRepository novelRepository;
    private final TtsVoiceRepository ttsVoiceRepository;
    private final FileUtil fileUtil;
    private final RestTemplate restTemplate;

    @Value("${naver.tts.api-key-id:}")
    private String naverApiKeyId;

    @Value("${naver.tts.api-key:}")
    private String naverApiKey;

    @Value("${elevenlabs.api-key:}")
    private String elevenLabsApiKey;

    private static final int MAX_CHARS_PER_REQUEST = 1500;
    private static final int NAVER_MAX_CHARS = 1000;
    private static final int ELEVENLABS_MAX_CHARS = 2500;
    private static final String ELEVENLABS_BASE_URL = "https://api.elevenlabs.io/v1/text-to-speech/";

    public TtsResponseDto generateTts(Long episodeId, Long voiceId) {
        Novel novel = novelRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("소설 텍스트를 찾을 수 없습니다."));

        if (novel.getContentText() == null || novel.getContentText().isBlank()) {
            throw new IllegalArgumentException("소설 내용이 없습니다.");
        }

        TtsVoice ttsVoice = ttsVoiceRepository.findById(voiceId)
                .orElseThrow(() -> new IllegalArgumentException("목소리를 찾을 수 없습니다."));

        byte[] audioData = synthesizeByProvider(ttsVoice, novel.getContentText());

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

    public TtsVoice addVoice(TtsVoice voice) {
        return ttsVoiceRepository.save(voice);
    }

    public TtsVoice updateVoice(Long voiceId, TtsVoice updated) {
        TtsVoice voice = ttsVoiceRepository.findById(voiceId)
                .orElseThrow(() -> new IllegalArgumentException("목소리를 찾을 수 없습니다."));
        voice.setVoiceName(updated.getVoiceName());
        voice.setVoiceType(updated.getVoiceType());
        voice.setVoiceStyle(updated.getVoiceStyle());
        voice.setIsDefault(updated.getIsDefault());
        if (updated.getSampleUrl() != null) voice.setSampleUrl(updated.getSampleUrl());
        if (updated.getFileUrl() != null) voice.setFileUrl(updated.getFileUrl());
        return ttsVoiceRepository.save(voice);
    }

    public void deleteVoice(Long voiceId) {
        ttsVoiceRepository.deleteById(voiceId);
    }

    public void batchGenerateTts(Long contentId, Long voiceId) {
        List<Episode> episodes = episodeRepository.findAllWithNovelByContentId(contentId);
        TtsVoice ttsVoice = ttsVoiceRepository.findById(voiceId)
                .orElseThrow(() -> new IllegalArgumentException("목소리를 찾을 수 없습니다."));

        for (Episode episode : episodes) {
            Novel novel = episode.getNovel();
            if (novel == null || novel.getContentText() == null || novel.getContentText().isBlank()) continue;

            byte[] audioData = synthesizeByProvider(ttsVoice, novel.getContentText());

            if (novel.getTtsFileUrl() != null) {
                fileUtil.deleteFromS3(novel.getTtsFileUrl());
            }

            String ttsFileUrl = fileUtil.uploadAudioToS3(audioData);
            novel.setTtsFileUrl(ttsFileUrl);
            novelRepository.save(novel);
        }
    }

    private byte[] synthesizeByProvider(TtsVoice voice, String text) {
        String provider = voice.getProvider() != null ? voice.getProvider() : "GOOGLE";
        return switch (provider) {
            case "NAVER" -> synthesizeNaver(text, voice.getVoiceName());
            case "ELEVENLABS" -> synthesizeElevenLabs(text, voice.getVoiceName());
            default -> {
                double pitch = voice.getPitch() != null ? voice.getPitch() : 0.0;
                double speakingRate = voice.getSpeakingRate() != null ? voice.getSpeakingRate() : 1.0;
                yield synthesize(text, voice.getVoiceName(), pitch, speakingRate);
            }
        };
    }

    private byte[] synthesizeElevenLabs(String text, String voiceId) {
        List<String> chunks = splitElevenLabsChunks(text);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (String chunk : chunks) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("xi-api-key", elevenLabsApiKey);
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Accept", "audio/mpeg");

                Map<String, Object> voiceSettings = new HashMap<>();
                voiceSettings.put("stability", 0.45);
                voiceSettings.put("similarity_boost", 0.80);
                voiceSettings.put("style", 0.35);
                voiceSettings.put("use_speaker_boost", true);

                Map<String, Object> body = new HashMap<>();
                body.put("text", chunk);
                body.put("model_id", "eleven_multilingual_v2");
                body.put("voice_settings", voiceSettings);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                ResponseEntity<byte[]> response = restTemplate.exchange(
                        ELEVENLABS_BASE_URL + voiceId + "?output_format=mp3_44100_128",
                        HttpMethod.POST, entity, byte[].class
                );

                if (response.getBody() != null) output.writeBytes(response.getBody());
            } catch (Exception e) {
                throw new RuntimeException("ElevenLabs TTS 합성 실패: " + e.getMessage(), e);
            }
        }
        return output.toByteArray();
    }

    private List<String> splitElevenLabsChunks(String text) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?\\n。])");
        StringBuilder current = new StringBuilder();
        for (String s : sentences) {
            if (current.length() + s.length() > ELEVENLABS_MAX_CHARS) {
                if (!current.isEmpty()) { chunks.add(current.toString()); current = new StringBuilder(); }
                if (s.length() > ELEVENLABS_MAX_CHARS) {
                    for (int i = 0; i < s.length(); i += ELEVENLABS_MAX_CHARS)
                        chunks.add(s.substring(i, Math.min(i + ELEVENLABS_MAX_CHARS, s.length())));
                    continue;
                }
            }
            current.append(s);
        }
        if (!current.isEmpty()) chunks.add(current.toString());
        return chunks;
    }

    private byte[] synthesizeNaver(String text, String voiceName) {
        List<String> chunks = splitNaverChunks(text);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (String chunk : chunks) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-NCP-APIGW-API-KEY-ID", naverApiKeyId);
                headers.set("X-NCP-APIGW-API-KEY", naverApiKey);
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                String body = "speaker=" + voiceName + "&volume=0&speed=0&pitch=0&format=mp3&text="
                        + URLEncoder.encode(chunk, StandardCharsets.UTF_8);

                HttpEntity<String> entity = new HttpEntity<>(body, headers);
                ResponseEntity<byte[]> response = restTemplate.exchange(
                        "https://naveropenapi.apigw.ntruss.com/tts-premium/v1/tts",
                        HttpMethod.POST, entity, byte[].class
                );

                if (response.getBody() != null) output.writeBytes(response.getBody());
            } catch (Exception e) {
                throw new RuntimeException("Naver TTS 합성 실패: " + e.getMessage(), e);
            }
        }
        return output.toByteArray();
    }

    private List<String> splitNaverChunks(String text) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?\\n])");
        StringBuilder current = new StringBuilder();
        for (String s : sentences) {
            if (current.length() + s.length() > NAVER_MAX_CHARS) {
                if (!current.isEmpty()) { chunks.add(current.toString()); current = new StringBuilder(); }
                if (s.length() > NAVER_MAX_CHARS) {
                    for (int i = 0; i < s.length(); i += NAVER_MAX_CHARS)
                        chunks.add(s.substring(i, Math.min(i + NAVER_MAX_CHARS, s.length())));
                    continue;
                }
            }
            current.append(s);
        }
        if (!current.isEmpty()) chunks.add(current.toString());
        return chunks;
    }

    // 긴 텍스트를 청크로 나눠 순서대로 합성 후 MP3 바이트 배열로 반환
    private byte[] synthesize(String text, String voiceName, double pitch, double speakingRate) {
        List<String> chunks = splitIntoChunks(text);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("ko-KR")
                .setName(voiceName)
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .setPitch(pitch)
                .setSpeakingRate(speakingRate)
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
