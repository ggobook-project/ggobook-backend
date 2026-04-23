package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.enums.ReportReason;
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
        String reasonCode = request.get("reason");

        // 🌟 기타 사유일 경우 입력창 값을 사용
        String finalReason = reasonCode.equals("OTHER") ?
                request.get("customReason") :
                ReportReason.valueOf(reasonCode).getDescription();

        adminMemberService.suspendMember(6L, userId, duration, finalReason);
        return ResponseEntity.ok("정지 처리되었습니다.");
    }

    // 기존 suspendMember 아래에 추가
    @PostMapping("/{userId}/release")
    public ResponseEntity<String> releaseMember(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {

        String reason = request.getOrDefault("reason", "관리자 직권 정지 해제");

        // 실제 운영 시 로그인된 관리자 ID를 가져오는 로직으로 대체 (이전 논의 참조)
        Long adminId = 6L;

        adminMemberService.releaseMember(adminId, userId, reason);
        return ResponseEntity.ok("회원 정지가 성공적으로 해제되었습니다.");
    }

//    @PostMapping("/{userId}/point")
//    public ResponseEntity<String> adjustPoint(
//            @PathVariable Long userId,
//            @RequestBody Map<String, Integer> request) {
//
//        int amount = request.get("amount");
//        adminMemberService.adjustPoint(userId, amount);
//        return ResponseEntity.ok("포인트가 성공적으로 반영되었습니다.");
//    }
}