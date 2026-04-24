package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.RecentView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecentViewRepository extends JpaRepository<RecentView, Long> {

    Slice<RecentView> findByUserIdOrderByViewedAtDesc(Long userId, Pageable pageable);

    Optional<RecentView> findByUserIdAndContent_ContentId(Long userId, Long contentId);
}