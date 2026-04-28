package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tts_chunk", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"episode_id", "voice_id", "chunk_index"})
})
public class TtsChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chunkId;

    @Column(name = "episode_id", nullable = false)
    private Long episodeId;

    @Column(name = "voice_id", nullable = false)
    private Long voiceId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "chunk_url", nullable = false, length = 500)
    private String chunkUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
