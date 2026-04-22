package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Likes;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.dto.LikedContentDto; //  DTO 추가
import com.untitled.ggobook.repository.ContentRepository;
import com.untitled.ggobook.repository.LikeRepository;
import com.untitled.ggobook.repository.UserRepository; //  추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; //  추가
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository; //  토큰 아이디로 유저를 찾기 위해 추가

    @Transactional //  JPA  체킹을 위해 필수
    public void toggleLike(String loginId, Long contentId) { //  파라미터 변경: Long -> String(토큰)

        // 1. 토큰에서 추출한 아이디로 진짜 유저(Long id)를 찾습니다.
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 2. 기존 로직 그대로 유지하되 userId 대신 user.getId() 사용
        Likes existing = likeRepository.findByUserIdAndContent_ContentId(user.getId(), contentId);
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("작품 없음"));

        if(existing != null){
            likeRepository.delete(existing);
            content.setLikeCount(content.getLikeCount() - 1);
        } else {
            Likes likes = new Likes();
            likes.setContent(content);
            likes.setUserId(user.getId()); //  수정
            likeRepository.save(likes);
            content.setLikeCount(content.getLikeCount() + 1);
        }

        //  contentRepository.save(content); 삭제됨 ( 체킹으로 자동 저장)
    }

    @Transactional(readOnly = true) //  읽기 전용으로 성능 최적화
    public Slice<LikedContentDto> getLikedContentList(String loginId, Pageable pageable) {
        // 1. 유저 찾기
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 2. DB에서 찜 목록 엔티티 가져오기
        Slice<Likes> likesSlice = likeRepository.findByUserId(user.getId(), pageable);

        // 3. 엔티티(Likes)를 프론트엔드용 DTO(LikedContentDto)로 변환해서 반환
        return likesSlice.map(like -> LikedContentDto.from(like.getContent()));
    }
}
