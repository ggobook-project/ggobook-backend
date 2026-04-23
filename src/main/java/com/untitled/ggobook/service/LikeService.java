package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Likes;
import com.untitled.ggobook.dto.LikedContentDto;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.LikeRepository;
// 🌟 팩트: 더 이상 유저 DB 조회가 필요 없으므로 UserRepository import 완전 삭제!
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final ContentRepository contentRepository;

    @Transactional
    public void toggleLike(Long id, Long contentId) {

        // 유저 찾기(userRepository.findByUserId) 삭제 완료! 쿼리 1번 절약.

        //  컨트롤러에서 바로 넘어온 PK(id)를 다이렉트로 사용합니다.
        Likes existing = likeRepository.findByUserIdAndContent_ContentId(id, contentId);
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품 없음"));

        if(existing != null){
            likeRepository.delete(existing);
            content.setLikeCount(content.getLikeCount() - 1);
        } else {
            Likes likes = new Likes();
            likes.setContent(content);
            likes.setUserId(id); // 🌟 PK(id) 바로 꽂아 넣기
            likeRepository.save(likes);
            content.setLikeCount(content.getLikeCount() + 1);
        }
    }

    @Transactional(readOnly = true)
    public Slice<LikedContentDto> getLikedContentList(Long id, Pageable pageable) {


        //  PK(id)를 가지고 찜 창고(LikeRepository)에 바로 요청합니다.
        Slice<Likes> likesSlice = likeRepository.findByUserId(id, pageable);

        return likesSlice.map(like -> LikedContentDto.from(like.getContent()));
    }
}