package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.MultiVoiceChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MultiVoiceChunkRepository extends JpaRepository<MultiVoiceChunk, Long> {
    Optional<MultiVoiceChunk> findByEpisodeIdAndVoice1IdAndVoice2IdAndNarratorVoiceIdAndSegmentIndex(
            Long episodeId, Long voice1Id, Long voice2Id, Long narratorVoiceId, Integer segmentIndex);

    List<MultiVoiceChunk> findByEpisodeIdAndVoice1IdAndVoice2IdAndNarratorVoiceIdOrderBySegmentIndex(
            Long episodeId, Long voice1Id, Long voice2Id, Long narratorVoiceId);
}
