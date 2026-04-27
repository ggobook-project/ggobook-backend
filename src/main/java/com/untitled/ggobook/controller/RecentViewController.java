package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.RecentContentDto;
import com.untitled.ggobook.dto.SaveRecentViewRequest;
import com.untitled.ggobook.service.RecentViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recent-views")
@RequiredArgsConstructor
public class RecentViewController {

    private final RecentViewService recentViewService;

    // 1. 최근 본 작품 목록 조회 (무한 스크롤)
    @GetMapping
    public ResponseEntity<Slice<RecentContentDto>> getRecentViewList(
            @AuthenticationPrincipal Long id,
            Pageable pageable) { // 🌟 통일성: Pageable 객체 직접 받기

        Slice<RecentContentDto> response = recentViewService.getRecentViewList(id, pageable);
        return ResponseEntity.ok(response);
    }

    // 2. 최근 본 작품 저장 및 갱신 (뷰어에서 호출)
    @PostMapping
    public ResponseEntity<String> saveRecentView(
            @AuthenticationPrincipal Long id, // 🌟 통일성: Long id 바로 꺼내기
            @RequestBody SaveRecentViewRequest request) {

        recentViewService.saveRecentView(id, request);
        return ResponseEntity.ok("최근 본 작품 기록이 업데이트되었습니다."); // 🌟 통일성: String 메시지 응답
    }
}