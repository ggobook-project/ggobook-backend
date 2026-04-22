package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {
    // 특정 유저의 내역을 최신순으로 잘라서(Slice) 가져옵니다.
    Slice<Point> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}