package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.ReportRequestDTO;
import com.untitled.ggobook.service.UserReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class UserReportController {
    private final UserReportService userReportService;

    @PostMapping
    public ResponseEntity<String> submitReport(
            @AuthenticationPrincipal Long userId,
            @RequestBody ReportRequestDTO dto) {

        userReportService.createReport(userId, dto);
        return ResponseEntity.ok("신고가 정상적으로 접수되었습니다.");
    }
}
