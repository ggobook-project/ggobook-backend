package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.*;
import com.untitled.ggobook.repository.*;
import com.untitled.ggobook.util.AIRequestUtil; // 🌟 신규: AI 통신 유틸 추가
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRelayService {

    private final RelayNovelRepository relayNovelRepository;
    private final RelayTopicRepository relayTopicRepository;
    private final AdminRelayTopicRepository adminRelayTopicRepository;
    private final RelayGuidelineRepository guidelineRepository;

    // 🌟 신규: 블라인드 처리를 위한 의존성 추가
    private final RelayEntryRepository relayEntryRepository;
    private final AIRequestUtil aiRequestUtil;

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
        guideline.setId(1L);
        guideline.setContent(newContent);
        guidelineRepository.save(guideline);
    }

    // --- [2. 릴레이 소설 및 유저 주제 강제 관리] --- (변경 없음)
    @Transactional(readOnly = true) // 1. 트랜잭션 안에서 작업해야 지연 로딩 데이터를 가져올 수 있습니다.
    public RelayNovel getRelayNovelDetail(Long novelId) {
        // DB에서 소설 정보를 찾습니다.
        RelayNovel novel = relayNovelRepository.findById(novelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 소설을 찾을 수 없습니다."));

        // 🌟 [핵심 수정 부분] 지연 로딩된 entries를 강제로 깨웁니다 (초기화).
        // 리액트로 데이터를 던지기(JSON 변환) 전에,
        // "이 소설에 달린 이어쓰기 목록을 지금 당장 DB에서 다 가져와!"라고 명령하는 것입니다.
        if (novel.getEntries() != null) {
            novel.getEntries().size(); // .size()를 호출하는 것만으로도 JPA는 데이터를 실제로 읽어옵니다.
        }

        return novel;
    }

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

    @Transactional(readOnly = true)
    public List<RelayTopic> getUserTopicList() {
        return relayTopicRepository.findAll();
    }

    // --- [3. 관리자 공식 주제(AdminRelayTopic) 관리] --- (변경 없음)
    @Transactional(readOnly = true)
    public List<AdminRelayTopic> getAdminTopicList() {
        return adminRelayTopicRepository.findAll();
    }

    @Transactional
    public void registerAdminTopic(String title, String description) {
        AdminRelayTopic adminTopic = new AdminRelayTopic();
        adminTopic.setTitle(title);
        adminTopic.setDescription(description);
        adminRelayTopicRepository.save(adminTopic);
    }

    @Transactional
    public void deleteAdminTopic(Long adminTopicId) {
        adminRelayTopicRepository.deleteById(adminTopicId);
    }

    // ==========================================
    // 🌟 4. [신규 추가] 릴레이 소설 회차 강제 블라인드 (AI 스토리 브릿지)
    // ==========================================
    @Transactional
    public void blindRelayEpisode(Long entryId, String manualSummary) {
        RelayEntry entry = relayEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 릴레이 회차를 찾을 수 없습니다."));

        String finalSummary;

        // 1. 프론트엔드(관리자)가 직접 요약본을 적어 보냈다면 그것을 최우선 사용!
        if (manualSummary != null && !manualSummary.trim().isEmpty()) {
            finalSummary = manualSummary;
        }
        // 2. 안 적어 보냈다면 AI 전령(AIRequestUtil) 출동!
        else {
            // 🌟 복잡한 프롬프트 문자열 싹 지우고, 원문만 깔끔하게 넘깁니다!
            finalSummary = aiRequestUtil.requestRelaySummary(entry.getEntryText());
        }

        // 3. 상태 변경 및 요약본 저장
        entry.blind("[블라인드 요약]\n" + finalSummary);
    }
}