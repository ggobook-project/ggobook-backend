package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.RelayTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelayTopicRepository extends JpaRepository<RelayTopic, Long> {
    // 주제 추가(save), 삭제(deleteById) 등 기본 기능 사용
}