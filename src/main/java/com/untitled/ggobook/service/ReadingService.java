package com.untitled.ggobook.service;

import com.untitled.ggobook.repository.ReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReadingService {
    private final ReadingRepository readingRepository;


    @Transactional(readOnly = true)
    public List<Long> getReadEpisodeIds(Long userId, Long contentId) {
        return readingRepository.findByUserIdAndContent_ContentId(userId, contentId)
                .stream()
                .map(r -> r.getEpisode().getEpisodeId())
                .collect(Collectors.toList());
    }
}
