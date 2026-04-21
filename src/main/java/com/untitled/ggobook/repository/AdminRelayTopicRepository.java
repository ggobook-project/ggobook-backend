package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.AdminRelayTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRelayTopicRepository extends JpaRepository<AdminRelayTopic, Long> {
    // 공식 주제 관리를 위한 기본 CRUD 제공
}