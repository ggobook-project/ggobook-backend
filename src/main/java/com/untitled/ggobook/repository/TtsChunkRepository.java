package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.TtsChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TtsChunkRepository extends JpaRepository<TtsChunk, Long> {
    Optional<TtsChunk> findByEpisodeIdAndVoiceIdAndChunkIndex(Long episodeId, Long voiceId, Integer chunkIndex);
    List<TtsChunk> findByEpisodeIdAndVoiceIdOrderByChunkIndex(Long episodeId, Long voiceId);
    void deleteByEpisodeIdAndVoiceId(Long episodeId, Long voiceId);
}
