package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.TtsVoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TtsVoiceRepository extends JpaRepository<TtsVoice, Long> {
    List<TtsVoice> findByIsDefaultTrue();
}