package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Likes;
import com.untitled.ggobook.domain.User; // 🌟 추가
import com.untitled.ggobook.dto.LikedContentDto;
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.LikeRepository;
import com.untitled.ggobook.repository.UserRepository; // 🌟 작가 이름 찾기 위해 부활!
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
    private final UserRepository userRepository; // 🌟 의존성 주입 복구

    @Transactional
    public void toggleLike(Long id, Long contentId) {
        Likes existing = likeRepository.findByUserIdAndContent_ContentId(id, contentId);
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품 없음"));

        if(existing != null){
            likeRepository.delete(existing);
            content.setLikeCount(content.getLikeCount() - 1);
        } else {
            Likes likes = new Likes();
            likes.setContent(content);
            likes.setUserId(id);
            likeRepository.save(likes);
            content.setLikeCount(content.getLikeCount() + 1);
        }
    }

    @Transactional(readOnly = true)
    public Slice<LikedContentDto> getLikedContentList(Long id, Pageable pageable) {
        Slice<Likes> likesSlice = likeRepository.findByUserId(id, pageable);

        // 🌟 팩트: DTO가 이제 (Content, 작가이름) 2개를 원하므로, 유저 창고에서 찾아와서 넣어줍니다.
        return likesSlice.map(like -> {
            Content content = like.getContent();

            // 번호표(authorId)로 진짜 닉네임 찾아오기
            String authorName = userRepository.findById(content.getAuthor().getId())
                    .map(User::getNickname)
                    .orElse("알 수 없는 작가");

            // 두 개를 같이 던져줌!
            return LikedContentDto.from(content, authorName);
        });
    }
}