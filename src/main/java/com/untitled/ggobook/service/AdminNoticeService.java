package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Notice;
import com.untitled.ggobook.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    public void registerNotice(String title, String content, boolean isPinned, Long adminId) {
        Notice notice = Notice.builder()
                .title(title)
                .content(content)
                .isPinned(isPinned)
                .authorId(adminId)
                .build();
        noticeRepository.save(notice);
    }

    @Transactional
    public void updateNotice(Long noticeId, String title, String content, boolean isPinned) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));

        notice.update(title, content, isPinned);
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        noticeRepository.deleteById(noticeId);
    }
}