package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Likes;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.dto.LikedContentDto;
import com.untitled.ggobook.repository.LikeRepository;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyLikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Slice<LikedContentDto> getMyLikedContents(Long id, Pageable pageable) {
        Slice<Likes> likesSlice = likeRepository.findByUserId(id, pageable);

        return likesSlice.map(like -> {
            Content content = like.getContent();

            String authorName = "알 수 없는 작가";
            if (content.getAuthor() != null) {
                authorName = userRepository.findById(content.getAuthor().getId())
                        .map(User::getNickname)
                        .orElse("알 수 없는 작가");
            }

            return LikedContentDto.from(content, authorName);
        });
    }
}