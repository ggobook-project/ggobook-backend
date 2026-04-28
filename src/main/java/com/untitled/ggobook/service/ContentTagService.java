package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.ContentTag;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.ContentTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 작품 태그 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class ContentTagService {

    private final ContentTagRepository contentTagRepository;
    private final ContentRepository contentRepository;

    @Transactional(readOnly = true)
    public List<ContentTag> getTags(Long contentId) {
        return contentTagRepository.findByContent_ContentId(contentId);
    }

    @Transactional
    public void addTag(Long contentId, String tagName) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("작품이 존재하지 않습니다."));

        ContentTag tag = new ContentTag();
        tag.setContent(content);
        tag.setTagName(tagName);
        System.out.println("추가된 태그 : "+ tagName);
        contentTagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(Long contentId, Long tagId) {
        contentTagRepository.deleteByTagIdAndContent_ContentId(tagId, contentId);
    }
}
