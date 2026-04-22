package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Likes;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 찜 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final ContentRepository contentRepository;


    @Transactional
    public void toggleLike(Long userId, Long contentId) {

        Likes existing = likeRepository.findByUserIdAndContent_ContentId(userId, contentId);
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품 없음"));

        if(existing != null){
            likeRepository.delete(existing);
            content.setLikeCount(content.getLikeCount() - 1);
        }else{
            Likes likes = new Likes();

            likes.setContent(content);
            likes.setUserId(userId);

            likeRepository.save(likes);
            content.setLikeCount(content.getLikeCount() + 1);

        }

        contentRepository.save(content);

    }

    public Slice<Likes> getLikedContentList(Long userId, Pageable pageable) {
        return likeRepository.findByUserId(userId, pageable);
    }
}
