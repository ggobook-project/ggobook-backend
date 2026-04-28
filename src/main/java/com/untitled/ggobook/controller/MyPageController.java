package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.LikedContentDto;
import com.untitled.ggobook.dto.MyProfileResponse;
import com.untitled.ggobook.dto.UpdateMyInfoRequest;
import com.untitled.ggobook.dto.MyActivityDto;
import com.untitled.ggobook.dto.MyRelayNovelDto;
import com.untitled.ggobook.service.MyActivityService;
import com.untitled.ggobook.service.MyLikeService;
import com.untitled.ggobook.service.MyProfileService;
import com.untitled.ggobook.service.MyRelayNovelService;
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

    // 🌟 갓 오브젝트(MyPageService) 1개를 버리고 전문 서비스 4개로 교체!
    private final MyProfileService myProfileService;
    private final MyLikeService myLikeService;
    private final MyActivityService myActivityService;
    private final MyRelayNovelService myRelayNovelService;

    // 1. 내 정보 및 작성 글 목록 조회
    @GetMapping
    public ResponseEntity<MyProfileResponse> getMyInfo(@AuthenticationPrincipal Long id) {
        MyProfileResponse response = myProfileService.getMyProfile(id);
        return ResponseEntity.ok(response);
    }

    // 2. 내 정보 수정
    @PutMapping
    public ResponseEntity<String> updateMyInfo(
            @AuthenticationPrincipal Long id,
            @RequestBody UpdateMyInfoRequest request) {
        myProfileService.updateMyInfo(id, request);
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
    }

    // 3. 나의 찜 목록 조회 API
    @GetMapping("/likes")
    public ResponseEntity<Slice<LikedContentDto>> getMyLikes(
            @AuthenticationPrincipal Long id,
            Pageable pageable) {
        Slice<LikedContentDto> response = myLikeService.getMyLikedContents(id, pageable);
        return ResponseEntity.ok(response);
    }

    // 4. 내가 작성한 댓글/답글 통합 조회 API
    @GetMapping("/comments")
    public ResponseEntity<List<MyActivityDto>> getMyComments(
            @AuthenticationPrincipal Long id,
            Pageable pageable) {
        List<MyActivityDto> response = myActivityService.getMyAllCommentsAndReplies(id, pageable);
        return ResponseEntity.ok(response);
    }

    // 5. 내 릴레이 소설 목록 조회 API
    @GetMapping("/relay-novels")
    public ResponseEntity<List<MyRelayNovelDto>> getMyRelayNovels(
            @AuthenticationPrincipal Long id) {
        List<MyRelayNovelDto> response = myRelayNovelService.getMyRelayNovels(id);
        return ResponseEntity.ok(response);
    }
}