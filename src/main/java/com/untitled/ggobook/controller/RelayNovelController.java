package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.RelayNovel;
import com.untitled.ggobook.dto.RelayNovelCreateRequestDTO;
import com.untitled.ggobook.dto.RelayNovelDTO;
import com.untitled.ggobook.dto.RelayNovelListDTO;
import com.untitled.ggobook.service.RelayNovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relay-novels") // 🌟 관리자용과 구분되는 퍼블릭 경로
@RequiredArgsConstructor
public class RelayNovelController {

    private final RelayNovelService relayNovelService;

    // 목록 조회 (sort 파라미터로 최신/인기순 구분)
    // 예: /api/relay-novels?sort=popular&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<RelayNovelListDTO>> getRelayNovels(
            @RequestParam(defaultValue = "latest") String sortType,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(relayNovelService.getRelayNovels(sortType, pageable));
    }

    // 상세 조회
    @GetMapping("/{novelId}")
    public ResponseEntity<RelayNovelDTO> getRelayNovelDetail(@PathVariable Long novelId) {
        return ResponseEntity.ok(relayNovelService.getRelayNovelDetail(novelId));
    }

    @PostMapping
    public ResponseEntity<Long> createRelayNovel(
            @RequestBody RelayNovelCreateRequestDTO dto,
            @AuthenticationPrincipal Long id) {

        Long novelId = relayNovelService.createRelayNovel(id, dto);
        return ResponseEntity.ok(novelId);
    }

    // ==========================================
    // 1. 이어쓰기 시작 (락 획득 시도)
    // ==========================================
    @PostMapping("/{novelId}/start")
    public ResponseEntity<String> startWriting(
            @PathVariable Long novelId,
            @AuthenticationPrincipal Long currentUserId // 🌟 JWT에서 자동으로 유저 ID를 가져옵니다!
    ) {
        try {
            relayNovelService.startWriting(novelId, currentUserId);
            return ResponseEntity.ok("이어쓰기 방에 입장했습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ==========================================
    // 2. 작성 완료 및 제출 (락 해제 + DB 저장)
    // ==========================================
    @PostMapping("/{novelId}/submit")
    public ResponseEntity<String> submitEpisode(
            @PathVariable Long novelId,
            @RequestBody java.util.Map<String, String> request, // 🌟 프론트에서 보낸 JSON 객체를 Map으로 받습니다!
            @AuthenticationPrincipal Long currentUserId
    ) {
        // 프론트엔드가 보낸 'entryText'라는 키값에서 내용을 쏙 빼옵니다.
        String content = request.get("entryText");

        relayNovelService.submitEpisode(novelId, currentUserId, content);
        return ResponseEntity.ok("성공적으로 회차가 등록되었습니다.");
    }

    // ==========================================
    // 3. 작성 취소 (락 즉시 해제)
    // ==========================================
    @PostMapping("/{novelId}/cancel")
    public ResponseEntity<String> cancelWriting(
            @PathVariable Long novelId,
            @AuthenticationPrincipal Long currentUserId // (옵션) 본인이 건 락만 풀게 하고 싶을 때 사용
    ) {
        relayNovelService.cancelWriting(novelId);
        return ResponseEntity.ok("작성이 취소되고 방이 열렸습니다.");
    }

}