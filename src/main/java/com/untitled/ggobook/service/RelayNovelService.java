package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.RelayNovel;
import com.untitled.ggobook.repository.RelayNovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RelayNovelService {

    private final RelayNovelRepository relayNovelRepository;

    // 1. 목록 조회 (정렬 분기 처리)
    @Transactional(readOnly = true)
    public Page<RelayNovel> getRelayNovels(String sortType, Pageable pageable) {
        if ("popular".equalsIgnoreCase(sortType)) {
            return relayNovelRepository.findAllOrderByEntryCountDescAndTitleAsc(pageable);
        }
        return relayNovelRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // 2. 상세 조회
    @Transactional(readOnly = true)
    public RelayNovel getRelayNovelDetail(Long novelId) {
        return relayNovelRepository.findById(novelId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 릴레이 소설입니다."));
    }
}
