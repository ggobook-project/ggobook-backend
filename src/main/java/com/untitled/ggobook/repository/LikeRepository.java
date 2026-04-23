package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Likes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    Likes findByUserIdAndContent_ContentId(Long userId, Long contentId);

    //  JOIN FETCH l.content 한 단어만 추가하면, 찜을 가져올 때 작품 정보까지 한 방에 다 퍼옵니다! 쿼리가 11번에서 1번으로 줄어듭니다.
    @Query("SELECT l FROM Likes l JOIN FETCH l.content WHERE l.userId = :userId")
    Slice<Likes> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
