package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.ContentTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentTagRepository extends JpaRepository<ContentTag, Long> {
    List<ContentTag> findByContent_ContentId(Long contentId);
    void deleteByTagIdAndContent_ContentId(Long tagId, Long contentId);
}
