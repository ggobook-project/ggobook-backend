package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Likes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    Likes findByUserIdAndContent_ContentId(Long userId, Long contentId);

    @Query("SELECT l FROM Likes l WHERE l.userId = :userId")
    Slice<Likes> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
