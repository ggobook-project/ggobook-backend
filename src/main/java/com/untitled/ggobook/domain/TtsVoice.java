package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "tts_voice")
public class TtsVoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voiceId;

    @Column(nullable = false, length = 100)
    private String voiceName;

    // MALE / FEMALE
    @Column(nullable = false, length = 20)
    private String voiceType;

    // CUTE / CALM / SERIOUS 등
    @Column(nullable = false, length = 50)
    private String voiceStyle;

    @Column(nullable = false, length = 500)
    private String sampleUrl;

    @Column(nullable = false, length = 500)
    private String fileUrl;

    // 기본 제공 목소리 여부
    @Column(nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 이 목소리를 사용 중인 TTS 설정 (양방향)
    @ToString.Exclude
    @OneToMany(mappedBy = "ttsVoice", cascade = CascadeType.ALL)
    private List<TtsEpisodeSetting> settings = new ArrayList<>();
}