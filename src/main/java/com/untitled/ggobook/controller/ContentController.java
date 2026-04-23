package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.dto.ContentDetailDto;
import com.untitled.ggobook.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/")
    public Slice<Content> getContentlist(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        return contentService.getContentList(keyword, genre, type, pageable);
    }

    // 🌟 대기업 정석: 시큐리티 인증(AuthenticationPrincipal)을 유지하되, 비회원 에러 완벽 차단
    @GetMapping("/{contentId}")
    public ResponseEntity<ContentDetailDto> getContentDetail(
            @PathVariable Long contentId,
            @AuthenticationPrincipal Object principal, // 🌟 Object로 받아 Type Casting 에러 방지
            Pageable pageable) {

        Long userId = null;

        // 🌟 들어온 정보가 숫자(Long)일 때만 userId에 담음. 비회원("anonymousUser")이면 자연스럽게 null로 통과.
        if (principal instanceof Long) {
            userId = (Long) principal;
        }

        // 서비스 단으로 안전하게 넘김 (로그인이면 숫자, 아니면 null)
        ContentDetailDto response = contentService.getContentDetail(contentId, userId, pageable, "APPROVED");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/")
    public ResponseEntity<String> registerContent(
            @RequestPart("content") Content content,
            @RequestPart("file") MultipartFile multipartFile) {
        contentService.registerContent(content, multipartFile);
        return ResponseEntity.ok("작품 업로드 성공");
    }

    @PutMapping("/{contentId}")
    public void updateContent(@PathVariable Long contentId,
                              @RequestPart("content") Content content,
                              @RequestPart("file") MultipartFile multipartFile){
        content.setContentId(contentId);
        contentService.updateContent(content, multipartFile);
    }

    @DeleteMapping("/{contentId}")
    public void deleteContent(@PathVariable Long contentId){
        contentService.deleteContent(contentId);
    }
}