package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Notice;
import com.untitled.ggobook.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public Page<Notice> getNoticeList(Pageable pageable) {
        // 앞서 논의한 상단 고정 + 최신순 정렬 적용
        return noticeRepository.findAllByOrderByIsPinnedDescCreatedAtDesc(pageable);
    }

    @Transactional
    public Notice getNoticeDetail(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));

        notice.incrementViewCount(); // 조회수 증가
        return notice;
    }
}