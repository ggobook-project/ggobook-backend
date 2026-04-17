package com.untitled.ggobook.repository;

// 작품 리포지토리
import com.untitled.ggobook.domain.Content;
import com.untitled.ggobook.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    // 기획서의 getInspectionList() 기능을 위한 메서드입니다.
    // 검수 대기(PENDING) 상태인 작품들만 DB에서 쏙 뽑아옵니다.
    // (JPA가 내부적으로 "SELECT * FROM content WHERE status = 'PENDING'" 쿼리를 실행합니다.)
    List<Content> findByStatus(Status status);

    // 특정 작가의 작품만 모아보는 기능이 필요할 때 사용합니다.
    List<Content> findByAuthorId(Long authorId);
}