package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.LikedContentDto;
import com.untitled.ggobook.dto.MyProfileResponse;
import com.untitled.ggobook.dto.UpdateMyInfoRequest;
import com.untitled.ggobook.dto.MyActivityDto;
import com.untitled.ggobook.dto.MyPointResponseDto;
import com.untitled.ggobook.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    // 1. 내 정보 및 작성 글 목록 조회
    @GetMapping
    public ResponseEntity<MyProfileResponse> getMyInfo(@AuthenticationPrincipal String userId) {
        MyProfileResponse response = myPageService.getMyProfile(userId);
        return ResponseEntity.ok(response);
    }

    // 2. 내 정보 수정
    @PutMapping
    public ResponseEntity<String> updateMyInfo(
            @AuthenticationPrincipal String userId,
            @RequestBody UpdateMyInfoRequest request) {
        myPageService.updateMyInfo(userId, request);
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
    }

    // 3. 나의 찜 목록 조회 API
    @GetMapping("/likes")
    public ResponseEntity<Slice<LikedContentDto>> getMyLikes(
            @AuthenticationPrincipal String userId,
            Pageable pageable) {
        Slice<LikedContentDto> response = myPageService.getMyLikedContents(userId, pageable);
        return ResponseEntity.ok(response);
    }

    // 4. 내가 작성한 댓글/답글 통합 조회 API
    @GetMapping("/comments")
    public ResponseEntity<List<MyActivityDto>> getMyComments(
            @AuthenticationPrincipal String userId,
            Pageable pageable) {
        List<MyActivityDto> response = myPageService.getMyAllCommentsAndReplies(userId, pageable);
        return ResponseEntity.ok(response);
    }

    // 5. 내 포인트 통합 조회 API
    @GetMapping("/points")
    public ResponseEntity<MyPointResponseDto> getMyPoints(
            @AuthenticationPrincipal String userId,
            Pageable pageable) {
        MyPointResponseDto response = myPageService.getMyPoints(userId, pageable);
        return ResponseEntity.ok(response);
    }
}