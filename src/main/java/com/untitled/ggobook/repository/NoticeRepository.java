package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // 고정된 공지를 우선순위로, 그다음 최신순으로 정렬하여 조회
    Page<Notice> findAllByOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);
}