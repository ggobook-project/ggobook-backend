package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Novel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NovelRepository extends JpaRepository<Novel, Long> {
}
