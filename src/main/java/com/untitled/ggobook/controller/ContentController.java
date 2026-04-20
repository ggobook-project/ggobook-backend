package com.untitled.ggobook.controller;


import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.dto.ContentDetailDto;
import com.untitled.ggobook.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// 작품 컨트롤러
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

    @GetMapping("/{contentId}")
    public ContentDetailDto getContentDetail(@PathVariable Long contentId, Pageable pageable, String currentNeedStatus) {
        return contentService.getContentDetail(contentId, pageable, currentNeedStatus);
    }

    @PostMapping("/")
    public ResponseEntity<String> registerContent(
            @RequestPart("content") Content content,
            @RequestPart("file")MultipartFile multipartFile) {
        contentService.registerContent(content, multipartFile);

        return ResponseEntity.ok("작품 업로드 성공");
    }

    @PutMapping("/{contentId}")
    public void updateContent(@PathVariable Long contentID,
                              @RequestParam("content") Content content,
                              @RequestParam("file") MultipartFile multipartFile){
        content.setContentId(contentID);
        contentService.updateContent(content, multipartFile);
    }

    @DeleteMapping("/{contentId}")
    public void deleteContent(@PathVariable Long contentId){
        contentService.deleteContent(contentId);
    }
}
