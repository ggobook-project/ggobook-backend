package com.untitled.ggobook.controller;

import com.untitled.ggobook.service.RelayGuidelineService; // (또는 AdminRelayService)
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// 🌟 관리자용(/api/admin/...)과 완전히 다르게, 일반 유저가 접근하는 주소로 설정합니다.
@RequestMapping("/api/relay-guideline")
@RequiredArgsConstructor
public class RelayGuidelineController {

    // 서비스는 기존에 만드신 서비스 클래스의 메서드(가이드라인 조회)를 가져와서 씁니다.
    private final RelayGuidelineService guidelineService;

    @GetMapping
    public ResponseEntity<String> getGuideline() {
        // 일반 유저에게는 오직 '조회' 결과만 넘겨줍니다.
        return ResponseEntity.ok(guidelineService.getGuidelineContent());
    }
}