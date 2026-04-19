package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.util.FileUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// 작품 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final FileUtil fileUtil;

    public Slice<Content> getContentList(String keyword, String genre, String type, Pageable pageable) {
        String searchKeyword = (keyword == null || keyword.isBlank()) ? null : keyword;
        String searchGenre = (genre == null || genre.isBlank()) ? null : genre;

        return contentRepository.findContentList(searchKeyword, searchGenre, type, pageable);
    }

    public Content getContentDetail(@PathVariable Long contentId) {
        return contentRepository.findById(contentId).orElse(null);
    }

    @Transactional
    public void registerContent(Content content, MultipartFile multipartFile) {
        if(!multipartFile.isEmpty()){
            content.setThumbnailUrl(fileUtil.uploadToS3(multipartFile));
        }
        contentRepository.save(content);
    }

    public void updateContent(Content content, MultipartFile multipartFile){
        if(contentRepository.existsById(content.getContentId())) {
            if (!multipartFile.isEmpty()) {
                fileUtil.deleteFromS3(content.getThumbnailUrl());
                content.setThumbnailUrl(fileUtil.uploadToS3(multipartFile));
            }
            contentRepository.save(content);
        }
    }

    @Transactional
    public void deleteContent(Long contentId){
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작품이 없습니다."));

        if (content.getThumbnailUrl() != null) {
            fileUtil.deleteFromS3(content.getThumbnailUrl());
        }
        contentRepository.deleteById(contentId);
    }



}
