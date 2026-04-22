package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.RelayEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelayEntryRepository extends JpaRepository<RelayEntry, Long> {

    // 특정 소설에 달린 회차들을 순서(entryOrder)대로 가져오기
    List<RelayEntry> findByRelayNovel_RelayNovelIdOrderByEntryOrderAsc(Long relayNovelId);

    // 특정 유저가 쓴 릴레이 회차 목록 조회
    List<RelayEntry> findByUserId(Long userId);
}