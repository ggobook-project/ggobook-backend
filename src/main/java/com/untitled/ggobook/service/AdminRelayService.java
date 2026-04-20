package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.*;
import com.untitled.ggobook.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRelayService {

    private final RelayNovelRepository relayNovelRepository;
    private final RelayTopicRepository relayTopicRepository;
    // 🌟 신규: 관리자 공식 주제 리포지토리 추가!
    private final AdminRelayTopicRepository adminRelayTopicRepository;
    private final RelayGuidelineRepository guidelineRepository;

    // --- [1. 가이드라인 관리] --- (변경 없음)

    @Transactional(readOnly = true)
    public String getRelayGuideline() {
        return guidelineRepository.findById(1L)
                .map(RelayGuideline::getContent)
                .orElse("등록된 가이드라인이 없습니다.");
    }

    @Transactional
    public void updateRelayGuideline(String newContent) {
        RelayGuideline guideline = guidelineRepository.findById(1L)
                .orElse(new RelayGuideline());
        // Auto-Increment 충돌을 막기 위해 1번으로 강제 고정!
        guideline.setId(1L);
        guideline.setContent(newContent);
        guidelineRepository.save(guideline);
    }

    // --- [2. 릴레이 소설 및 유저 주제 강제 관리] ---

    @Transactional(readOnly = true)
    public List<RelayNovel> getRelayNovelList() {
        return relayNovelRepository.findAll();
    }

    @Transactional
    public void deleteRelayNovel(Long relayNovelId) {
        if (!relayNovelRepository.existsById(relayNovelId)) {
            throw new IllegalArgumentException("존재하지 않는 릴레이 소설입니다.");
        }
        relayNovelRepository.deleteById(relayNovelId);
    }

    // (선택) 유저들이 만든 자유 주제를 조회하거나 불량 주제를 지우는 기능 유지
    @Transactional(readOnly = true)
    public List<RelayTopic> getUserTopicList() {
        return relayTopicRepository.findAll();
    }

    // --- [3. 관리자 공식 주제(AdminRelayTopic) 관리] --- (🌟 전면 개편)

    @Transactional(readOnly = true)
    public List<AdminRelayTopic> getAdminTopicList() {
        return adminRelayTopicRepository.findAll(); // 공식 주제 목록 가져오기
    }

    @Transactional
    public void registerAdminTopic(String title, String description) {
        // 기존의 RelayTopic이 아니라 AdminRelayTopic을 생성합니다.
        AdminRelayTopic adminTopic = new AdminRelayTopic();
        adminTopic.setTitle(title);
        adminTopic.setDescription(description);
        adminRelayTopicRepository.save(adminTopic);
    }

    @Transactional
    public void deleteAdminTopic(Long adminTopicId) {
        adminRelayTopicRepository.deleteById(adminTopicId);
    }
}