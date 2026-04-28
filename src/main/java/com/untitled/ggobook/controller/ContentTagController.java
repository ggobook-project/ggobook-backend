package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.ContentTag;
import com.untitled.ggobook.service.ContentService;
import com.untitled.ggobook.service.ContentTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contents/{contentId}/tags")
@RequiredArgsConstructor
public class ContentTagController {

    private final ContentTagService contentTagService;

    @GetMapping
    public ResponseEntity<List<ContentTag>> getTags(@PathVariable Long contentId) {
        return ResponseEntity.ok(contentTagService.getTags(contentId));
    }

    @PostMapping("/register")
    public ResponseEntity<String> addTag(
            @PathVariable Long contentId,
            @RequestParam String tagName
    ) {
        contentTagService.addTag(contentId, tagName);
        return ResponseEntity.ok("태그 추가 완료");
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<String> deleteTag(
            @PathVariable Long contentId,
            @PathVariable Long tagId
    ) {
        contentTagService.deleteTag(contentId, tagId);
        return ResponseEntity.ok("태그 삭제 완료");
    }
}
