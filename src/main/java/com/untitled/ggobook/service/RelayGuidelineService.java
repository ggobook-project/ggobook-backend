package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.RelayGuideline;
import com.untitled.ggobook.repository.RelayGuidelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RelayGuidelineService {

    private final RelayGuidelineRepository guidelineRepository;

    @Transactional(readOnly = true)
    public String getGuidelineContent() {
        // 🌟 수정: 무조건 1번 데이터(단일 데이터)만 찾아옵니다.
        return guidelineRepository.findById(1L)
                .map(RelayGuideline::getContent)
                .orElse("등록된 공식 가이드라인이 없습니다.");
    }
}