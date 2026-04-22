package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.AdminRelayTopic;
import com.untitled.ggobook.domain.RelayNovel;
import com.untitled.ggobook.domain.RelayTopic;
import com.untitled.ggobook.service.AdminRelayService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRelayController {

    private final AdminRelayService adminRelayService;

    // ==========================================
    // 1. 릴레이 소설 가이드라인 관리 API
    // ==========================================

    @GetMapping("/relay-guideline")
    public ResponseEntity<String> getRelayGuideline() {
        String content = adminRelayService.getRelayGuideline();
        return ResponseEntity.ok(content);
    }

    @PutMapping("/relay-guideline")
    public ResponseEntity<Void> updateRelayGuideline(@RequestBody GuidelineRequest request) {
        adminRelayService.updateRelayGuideline(request.getContent());
        return ResponseEntity.ok().build();
    }

    // ==========================================
    // 2. 릴레이 소설 및 유저 주제 모니터링 API
    // ==========================================

    /**
     * [조회] 릴레이 소설 전체 목록 가져오기
     */
    @GetMapping("/relay-novels")
    public ResponseEntity<List<RelayNovel>> getRelayNovelList() {
        List<RelayNovel> novels = adminRelayService.getRelayNovelList();
        return ResponseEntity.ok(novels);
    }

    /**
     * [삭제] 특정 릴레이 소설 강제 삭제
     */
    @DeleteMapping("/relay-novels/{relayNovelId}")
    public ResponseEntity<Void> deleteRelayNovel(@PathVariable Long relayNovelId) {
        adminRelayService.deleteRelayNovel(relayNovelId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 🌟 [신규 조회] 유저들이 개설한 자유 주제 목록 가져오기
     * API: GET /api/admin/user-topics
     */
    @GetMapping("/user-topics")
    public ResponseEntity<List<RelayTopic>> getUserTopicList() {
        List<RelayTopic> userTopics = adminRelayService.getUserTopicList();
        return ResponseEntity.ok(userTopics);
    }

    // ==========================================
    // 3. 관리자 공식 주제(AdminRelayTopic) 관리 API (🌟 개편됨)
    // ==========================================

    /**
     * [조회] 관리자가 등록한 모든 '공식 주제' 목록 가져오기
     */
    @GetMapping("/relay-topics")
    public ResponseEntity<List<AdminRelayTopic>> getAdminTopicList() {
        // 반환 타입이 RelayTopic -> AdminRelayTopic 으로 변경되었습니다.
        List<AdminRelayTopic> topics = adminRelayService.getAdminTopicList();
        return ResponseEntity.ok(topics);
    }

    /**
     * [생성] 새로운 '공식 주제' 등록
     */
    @PostMapping("/relay-topics")
    public ResponseEntity<Void> registerAdminTopic(@RequestBody AdminTopicRequest request) {
        // 리액트에서 보낸 title과 description을 서비스로 넘깁니다.
        adminRelayService.registerAdminTopic(request.getTitle(), request.getDescription());
        return ResponseEntity.ok().build();
    }

    /**
     * [삭제] 지정된 '공식 주제' 삭제
     */
    @DeleteMapping("/relay-topics/{adminTopicId}")
    public ResponseEntity<Void> deleteAdminTopic(@PathVariable Long adminTopicId) {
        adminRelayService.deleteAdminTopic(adminTopicId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 릴레이 회차 강제 블라인드 API
    // ==========================================
    @PostMapping("/api/admin/relay/{entryId}/blind")
    public ResponseEntity<?> blindRelay(@PathVariable Long entryId, @RequestBody Map<String, String> request) {
        String manualSummary = request.get("manualSummary");

        // 🌟 여기가 중요합니다!
        // 서비스 메서드 이름과 파라미터 순서/개수가 일치해야 합니다.
        adminRelayService.blindRelayEpisode(entryId, manualSummary);

        return ResponseEntity.ok().build();
    }

    // ==========================================
    // Request DTO (클라이언트가 보내는 데이터를 담는 상자)
    // ==========================================

    @Data
    public static class GuidelineRequest {
        private String content;
    }

    // 🌟 [수정] 리액트에서 제목(title)과 설명(description)을 따로 보내도록 상자 구조 변경
    @Data
    public static class AdminTopicRequest {
        private String title;
        private String description;
    }
}