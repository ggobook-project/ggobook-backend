package com.untitled.ggobook.service;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.MultiVoiceChunk;
import com.untitled.ggobook.domain.Novel;
import com.untitled.ggobook.domain.TtsChunk;
import com.untitled.ggobook.domain.TtsVoice;
import com.untitled.ggobook.dto.TtsResponseDto;
import com.untitled.ggobook.repository.EpisodeRepository;
import com.untitled.ggobook.repository.MultiVoiceChunkRepository;
import com.untitled.ggobook.repository.NovelRepository;
import com.untitled.ggobook.repository.TtsChunkRepository;
import com.untitled.ggobook.repository.TtsVoiceRepository;
import com.untitled.ggobook.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final TtsChunkRepository ttsChunkRepository;
    private final MultiVoiceChunkRepository multiVoiceChunkRepository;
    private final FileUtil fileUtil;
    private final RestTemplate restTemplate;

    private static class Segment {
        String text;
        int speakerIndex; // -1=narrator, 0=speaker1, 1=speaker2
        Segment(String text, int speakerIndex) { this.text = text; this.speakerIndex = speakerIndex; }
    }

    @Value("${naver.tts.api-key-id:}")
    private String naverApiKeyId;

    @Value("${naver.tts.api-key:}")
    private String naverApiKey;

    @Value("${elevenlabs.api-key:}")
    private String elevenLabsApiKey;

    @Value("${typecast.api-key:}")
    private String typecastApiKey;

    private static final int MAX_CHARS_PER_REQUEST = 1500;
    private static final int NAVER_MAX_CHARS = 1000;
    private static final int ELEVENLABS_MAX_CHARS = 2500;
    private static final int TYPECAST_MAX_CHARS = 2000;
    private static final int CHUNK_TEXT_SIZE = 800;
    private static final String ELEVENLABS_BASE_URL = "https://api.elevenlabs.io/v1/text-to-speech/";
    private static final String TYPECAST_TTS_URL = "https://api.typecast.ai/v1/text-to-speech";
    private static final String TYPECAST_VOICES_URL = "https://api.typecast.ai/v2/voices";

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

    // 청크 정보 조회 (totalChunks + 이미 생성된 청크 URL 목록)
    @Transactional(readOnly = true)
    public Map<String, Object> getChunkInfo(Long episodeId, Long voiceId) {
        Novel novel = novelRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("소설을 찾을 수 없습니다."));
        String text = novel.getContentText();
        List<String> chunks = splitTextForChunking(text != null ? text : "");
        int totalChunks = chunks.size();

        List<TtsChunk> existing = ttsChunkRepository.findByEpisodeIdAndVoiceIdOrderByChunkIndex(episodeId, voiceId);
        Map<String, String> chunkUrls = new HashMap<>();
        for (TtsChunk c : existing) {
            chunkUrls.put(String.valueOf(c.getChunkIndex()), c.getChunkUrl());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalChunks", totalChunks);
        result.put("chunkUrls", chunkUrls);
        return result;
    }

    // 특정 청크 생성 (캐시 있으면 재사용)
    public String generateTtsChunk(Long episodeId, Long voiceId, Integer chunkIndex) {
        // 이미 생성된 청크면 바로 반환
        return ttsChunkRepository.findByEpisodeIdAndVoiceIdAndChunkIndex(episodeId, voiceId, chunkIndex)
                .map(TtsChunk::getChunkUrl)
                .orElseGet(() -> {
                    Novel novel = novelRepository.findById(episodeId)
                            .orElseThrow(() -> new IllegalArgumentException("소설을 찾을 수 없습니다."));
                    TtsVoice ttsVoice = ttsVoiceRepository.findById(voiceId)
                            .orElseThrow(() -> new IllegalArgumentException("목소리를 찾을 수 없습니다."));

                    List<String> chunks = splitTextForChunking(novel.getContentText());
                    if (chunkIndex >= chunks.size()) throw new IllegalArgumentException("청크 인덱스 초과");

                    byte[] audioData = synthesizeByProvider(ttsVoice, chunks.get(chunkIndex));
                    String url = fileUtil.uploadAudioToS3(audioData);

                    TtsChunk chunk = new TtsChunk();
                    chunk.setEpisodeId(episodeId);
                    chunk.setVoiceId(voiceId);
                    chunk.setChunkIndex(chunkIndex);
                    chunk.setChunkUrl(url);
                    ttsChunkRepository.save(chunk);

                    return url;
                });
    }

    // 텍스트를 CHUNK_TEXT_SIZE 이하의 청크로 분리 (문장 경계 기준)
    private List<String> splitTextForChunking(String text) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?\\n。])");
        StringBuilder current = new StringBuilder();
        for (String s : sentences) {
            if (current.length() + s.length() > CHUNK_TEXT_SIZE) {
                if (!current.isEmpty()) { chunks.add(current.toString()); current = new StringBuilder(); }
                if (s.length() > CHUNK_TEXT_SIZE) {
                    for (int i = 0; i < s.length(); i += CHUNK_TEXT_SIZE)
                        chunks.add(s.substring(i, Math.min(i + CHUNK_TEXT_SIZE, s.length())));
                    continue;
                }
            }
            current.append(s);
        }
        if (!current.isEmpty()) chunks.add(current.toString());
        return chunks;
    }

    // 텍스트를 대화/서술 세그먼트로 분리
    private List<Segment> parseMultiVoiceSegments(String text) {
        List<Segment> segments = new ArrayList<>();
        // "..." (ASCII), "..." (Unicode curly), 「...」, 『...』 모두 대화로 인식
        Pattern pattern = Pattern.compile(
            "\"[^\"]*\"|\\u201C[^\\u201D]*\\u201D|\\u300C[^\\u300D]*\\u300D|\\u300E[^\\u300F]*\\u300F",
            Pattern.DOTALL
        );
        Matcher m = pattern.matcher(text);
        int pos = 0;
        int dialogueCounter = 0;

        while (m.find()) {
            if (m.start() > pos) {
                String narration = text.substring(pos, m.start()).trim();
                if (!narration.isBlank()) {
                    for (String chunk : splitTextForChunking(narration))
                        segments.add(new Segment(chunk, -1));
                }
            }
            String dialogue = m.group(0).trim();
            if (!dialogue.isBlank()) {
                for (String chunk : splitTextForChunking(dialogue))
                    segments.add(new Segment(chunk, dialogueCounter % 2));
                dialogueCounter++;
            }
            pos = m.end();
        }

        if (pos < text.length()) {
            String remaining = text.substring(pos).trim();
            if (!remaining.isBlank()) {
                for (String chunk : splitTextForChunking(remaining))
                    segments.add(new Segment(chunk, -1));
            }
        }
        return segments;
    }

    // 멀티보이스 청크 정보 조회
    @Transactional(readOnly = true)
    public Map<String, Object> getMultiVoiceChunkInfo(Long episodeId, Long voice1Id, Long voice2Id, Long narratorVoiceId) {
        Novel novel = novelRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("소설을 찾을 수 없습니다."));
        String text = novel.getContentText() != null ? novel.getContentText() : "";
        List<Segment> segments = parseMultiVoiceSegments(text);

        List<MultiVoiceChunk> existing = multiVoiceChunkRepository
                .findByEpisodeIdAndVoice1IdAndVoice2IdAndNarratorVoiceIdOrderBySegmentIndex(
                        episodeId, voice1Id, voice2Id, narratorVoiceId);
        Map<String, String> chunkUrls = new HashMap<>();
        for (MultiVoiceChunk c : existing)
            chunkUrls.put(String.valueOf(c.getSegmentIndex()), c.getChunkUrl());

        Map<String, Object> result = new HashMap<>();
        result.put("totalChunks", segments.size());
        result.put("chunkUrls", chunkUrls);
        return result;
    }

    // 멀티보이스 특정 청크 생성 (캐시 있으면 재사용)
    public String generateMultiVoiceChunk(Long episodeId, Integer segmentIndex, Long voice1Id, Long voice2Id, Long narratorVoiceId) {
        return multiVoiceChunkRepository.findByEpisodeIdAndVoice1IdAndVoice2IdAndNarratorVoiceIdAndSegmentIndex(
                episodeId, voice1Id, voice2Id, narratorVoiceId, segmentIndex)
                .map(MultiVoiceChunk::getChunkUrl)
                .orElseGet(() -> {
                    Novel novel = novelRepository.findById(episodeId)
                            .orElseThrow(() -> new IllegalArgumentException("소설을 찾을 수 없습니다."));
                    List<Segment> segments = parseMultiVoiceSegments(novel.getContentText());
                    if (segmentIndex >= segments.size()) throw new IllegalArgumentException("세그먼트 인덱스 초과");

                    Segment seg = segments.get(segmentIndex);
                    Long voiceId = seg.speakerIndex == -1 ? narratorVoiceId
                            : seg.speakerIndex == 0 ? voice1Id : voice2Id;

                    TtsVoice ttsVoice = ttsVoiceRepository.findById(voiceId)
                            .orElseThrow(() -> new IllegalArgumentException("목소리를 찾을 수 없습니다."));

                    byte[] audioData = synthesizeByProvider(ttsVoice, seg.text);
                    String url = fileUtil.uploadAudioToS3(audioData);

                    MultiVoiceChunk chunk = new MultiVoiceChunk();
                    chunk.setEpisodeId(episodeId);
                    chunk.setVoice1Id(voice1Id);
                    chunk.setVoice2Id(voice2Id);
                    chunk.setNarratorVoiceId(narratorVoiceId);
                    chunk.setSegmentIndex(segmentIndex);
                    chunk.setChunkUrl(url);
                    multiVoiceChunkRepository.save(chunk);

                    return url;
                });
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
            case "TYPECAST" -> synthesizeTypecast(text, voice.getVoiceName());
            default -> {
                double pitch = voice.getPitch() != null ? voice.getPitch() : 0.0;
                double speakingRate = voice.getSpeakingRate() != null ? voice.getSpeakingRate() : 1.0;
                yield synthesize(text, voice.getVoiceName(), pitch, speakingRate);
            }
        };
    }

    private byte[] synthesizeTypecast(String text, String voiceId) {
        List<String> chunks = splitTypecastChunks(text);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        String formattedVoiceId = voiceId.startsWith("tc_") ? voiceId : "tc_" + voiceId;

        for (String chunk : chunks) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-API-KEY", typecastApiKey);
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> outputSettings = new HashMap<>();
                outputSettings.put("format", "mp3");
                outputSettings.put("volume", 100);

                Map<String, Object> body = new HashMap<>();
                body.put("voice_id", formattedVoiceId);
                body.put("text", chunk);
                body.put("model", "ssfm-v21");
                body.put("lang", "kor");
                body.put("output", outputSettings);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                ResponseEntity<byte[]> response = restTemplate.exchange(
                        TYPECAST_TTS_URL, HttpMethod.POST, entity, byte[].class
                );

                if (response.getBody() != null) output.writeBytes(response.getBody());

            } catch (Exception e) {
                throw new RuntimeException("Typecast TTS 합성 실패: " + e.getMessage(), e);
            }
        }
        return output.toByteArray();
    }

    private List<String> splitTypecastChunks(String text) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?\\n。])");
        StringBuilder current = new StringBuilder();
        for (String s : sentences) {
            if (current.length() + s.length() > TYPECAST_MAX_CHARS) {
                if (!current.isEmpty()) { chunks.add(current.toString()); current = new StringBuilder(); }
                if (s.length() > TYPECAST_MAX_CHARS) {
                    for (int i = 0; i < s.length(); i += TYPECAST_MAX_CHARS)
                        chunks.add(s.substring(i, Math.min(i + TYPECAST_MAX_CHARS, s.length())));
                    continue;
                }
            }
            current.append(s);
        }
        if (!current.isEmpty()) chunks.add(current.toString());
        return chunks;
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
