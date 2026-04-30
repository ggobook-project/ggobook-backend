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

import java.util.Map;

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
            // 🌟 핵심 수술: 프론트에서 인기순/최신순을 고를 수 있게 무전기를 뚫어줍니다. 기본값은 최신순(latest)
            @RequestParam(required = false, defaultValue = "latest") String sortType,
            Pageable pageable) {
        // 서비스로 sortType도 같이 넘겨줍니다!
        return contentService.getContentList(keyword, genre, type, sortType, pageable);
    }

    @GetMapping("/my")
    public ResponseEntity<Slice<Content>> getMyContents(
            @AuthenticationPrincipal Long id,
            Pageable pageable
    ) {
        return ResponseEntity.ok(contentService.getMyContents(id, pageable));
    }

    // 🌟 대기업 정석: 시큐리티 인증(AuthenticationPrincipal)을 유지하되, 비회원 에러 완벽 차단
    @GetMapping("/{contentId}")
    public ResponseEntity<ContentDetailDto> getContentDetail(
            @PathVariable Long contentId,
            @AuthenticationPrincipal Object principal,
            Pageable pageable) {

        Long userId = null;
        if (principal instanceof Long) {
            userId = (Long) principal;
        }

        ContentDetailDto response = contentService.getContentDetail(contentId, userId, pageable, "APPROVED");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/")
    public ResponseEntity<Map<String, Object>> registerContent(
            @RequestPart("content") Content content,
            @RequestPart("file") MultipartFile multipartFile,
            @AuthenticationPrincipal Long id) {
        Content saved = contentService.registerContent(content, multipartFile, id);
        return ResponseEntity.ok(Map.of("contentId", saved.getContentId()));
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