package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.enums.SuspensionDuration;
import com.untitled.ggobook.dto.UserAdminResponseDto;
import com.untitled.ggobook.service.AdminMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    @GetMapping
    public ResponseEntity<Page<UserAdminResponseDto>> getMemberList(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminMemberService.getMemberList(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserAdminResponseDto>> searchMember(
            @RequestParam String type,
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminMemberService.searchMember(type, keyword, pageable));
    }

    @PostMapping("/{userId}/suspend")
    public ResponseEntity<String> suspendMember(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {

        SuspensionDuration duration = SuspensionDuration.valueOf(request.get("duration"));
        String reason = request.get("reason");

        // 실제 운영 시 로그인된 관리자 ID를 가져오는 로직으로 대체 필요
        Long adminId = 1L;

        adminMemberService.suspendMember(adminId, userId, duration, reason);
        return ResponseEntity.ok("회원이 성공적으로 정지되었습니다.");
    }

    @PostMapping("/{userId}/point")
    public ResponseEntity<String> adjustPoint(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> request) {

        int amount = request.get("amount");
        adminMemberService.adjustPoint(userId, amount);
        return ResponseEntity.ok("포인트가 성공적으로 반영되었습니다.");
    }
}