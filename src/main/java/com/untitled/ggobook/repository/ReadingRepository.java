package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Episode;
import com.untitled.ggobook.domain.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReadingRepository extends JpaRepository<Reading, Long> {
    Optional<Reading> findByUserIdAndContent(Long id, Content content);
}
