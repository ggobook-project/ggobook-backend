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
     * 🌟 [신규 추가] 릴레이 소설 상세 조회 (이것이 없어서 405 에러가 났습니다!)
     * API: GET /api/admin/relay-novels/{novelId}
     */
    @GetMapping("/relay-novels/{novelId}")
    public ResponseEntity<RelayNovel> getRelayNovelDetail(@PathVariable Long novelId) {
        // 서비스 단에 getRelayNovelDetail(novelId) 메서드가 있어야 합니다.
        // 해당 메서드는 novelId로 소설을 찾아서 리턴해주는 역할을 합니다.
        RelayNovel novel = adminRelayService.getRelayNovelDetail(novelId);
        return ResponseEntity.ok(novel);
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
     * [조회] 유저들이 개설한 자유 주제 목록 가져오기
     */
    @GetMapping("/user-topics")
    public ResponseEntity<List<RelayTopic>> getUserTopicList() {
        List<RelayTopic> userTopics = adminRelayService.getUserTopicList();
        return ResponseEntity.ok(userTopics);
    }

    // ==========================================
    // 3. 관리자 공식 주제(AdminRelayTopic) 관리 API
    // ==========================================

    @GetMapping("/relay-topics")
    public ResponseEntity<List<AdminRelayTopic>> getAdminTopicList() {
        List<AdminRelayTopic> topics = adminRelayService.getAdminTopicList();
        return ResponseEntity.ok(topics);
    }

    @PostMapping("/relay-topics")
    public ResponseEntity<Void> registerAdminTopic(@RequestBody AdminTopicRequest request) {
        adminRelayService.registerAdminTopic(request.getTitle(), request.getDescription());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/relay-topics/{adminTopicId}")
    public ResponseEntity<Void> deleteAdminTopic(@PathVariable Long adminTopicId) {
        adminRelayService.deleteAdminTopic(adminTopicId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 4. 🌟 [수정됨] 릴레이 회차 강제 블라인드 API
    // ==========================================
    // 1. 리액트에서 PUT으로 보내므로 @PutMapping으로 변경
    // 2. 클래스 상단의 /api/admin 과 합쳐져서 최종 주소가 /api/admin/relay-entries/{entryId}/blind 가 됩니다.
    @PutMapping("/relay-entries/{entryId}/blind")
    public ResponseEntity<?> blindRelay(@PathVariable Long entryId, @RequestBody Map<String, String> request) {
        // 3. 리액트에서 "adminMessage" 라는 이름표로 데이터를 보내므로 그것을 꺼냅니다.
        String adminMessage = request.get("adminMessage");

        // 서비스 호출 (서비스 단의 파라미터는 기존에 작성하신 대로 넘깁니다)
        adminRelayService.blindRelayEpisode(entryId, adminMessage);

        return ResponseEntity.ok().build();
    }

    // ==========================================
    // Request DTO
    // ==========================================

    @Data
    public static class GuidelineRequest {
        private String content;
        private String adminId;
    }

    @Data
    public static class AdminTopicRequest {
        private String title;
        private String description;
    }
}