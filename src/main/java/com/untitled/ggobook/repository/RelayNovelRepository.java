package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.RelayNovel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelayNovelRepository extends JpaRepository<RelayNovel, Long> {
    // 전체 목록 조회(findAll), 삭제(deleteById) 등 기본 기능 사용

}