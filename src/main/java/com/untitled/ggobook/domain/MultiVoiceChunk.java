package com.untitled.ggobook.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "multi_voice_chunk", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"episode_id", "voice1_id", "voice2_id", "narrator_voice_id", "segment_index"})
})
public class MultiVoiceChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "episode_id", nullable = false)
    private Long episodeId;

    @Column(name = "voice1_id", nullable = false)
    private Long voice1Id;

    @Column(name = "voice2_id", nullable = false)
    private Long voice2Id;

    @Column(name = "narrator_voice_id", nullable = false)
    private Long narratorVoiceId;

    @Column(name = "segment_index", nullable = false)
    private Integer segmentIndex;

    @Column(name = "chunk_url", nullable = false, length = 500)
    private String chunkUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
