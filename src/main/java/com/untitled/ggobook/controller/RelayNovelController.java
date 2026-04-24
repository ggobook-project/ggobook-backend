package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.RelayNovel;
import com.untitled.ggobook.service.RelayNovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relay-novels") // 🌟 관리자용과 구분되는 퍼블릭 경로
@RequiredArgsConstructor
public class RelayNovelController {

    private final RelayNovelService relayNovelService;

    // 목록 조회 (sort 파라미터로 최신/인기순 구분)
    // 예: /api/relay-novels?sort=popular&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<RelayNovel>> getRelayNovels(
            @RequestParam(defaultValue = "latest") String sortType,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(relayNovelService.getRelayNovels(sortType, pageable));
    }

    // 상세 조회
    @GetMapping("/{novelId}")
    public ResponseEntity<RelayNovel> getRelayNovelDetail(@PathVariable Long novelId) {
        return ResponseEntity.ok(relayNovelService.getRelayNovelDetail(novelId));
    }
}