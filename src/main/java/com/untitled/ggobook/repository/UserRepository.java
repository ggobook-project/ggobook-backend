package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);

    // 중복 확인용 마법의 메서드 3개 추가
    boolean existsByUserId(String userId);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    // ==========================================
    //  설계도 기반 조회 로직
    // ==========================================
    Optional<User> findByNameAndEmail(String name, String email);
    Optional<User> findByEmail(String email);

    // 3. [스케줄러용] 현재 시간이 '정지 해제일'을 지났고, 상태가 SUSPENDED인 유저 찾기
    List<User> findByStatusAndSuspensionEndDateBefore(UserStatus status, LocalDateTime now);

    // ==========================================
    // 🌟 [수정] 관리자(ADMIN) 제외 검색 로직 추가
    // ※ 주의: User 엔티티의 role 타입에 따라 String 자리에 Role Enum을 써야 할 수도 있습니다.
    // ==========================================

    // 0. 전체 목록 조회용 (관리자 제외)
    Page<User> findByRoleNot(String role, Pageable pageable);

    // 1. [아이디 + 닉네임] 전체 검색용 (JPQL을 사용하여 가독성 확보)
    @Query("SELECT u FROM User u WHERE (u.userId LIKE %:keyword% OR u.nickname LIKE %:keyword%) AND u.role != :role")
    Page<User> searchAllKeywordAndRoleNot(@Param("keyword") String keyword, @Param("role") String role, Pageable pageable);

    // 2. [아이디]로만 검색 (관리자 제외)
    Page<User> findByUserIdContainingAndRoleNot(String userId, String role, Pageable pageable);

    // 3. [닉네임]으로만 검색 (관리자 제외)
    Page<User> findByNicknameContainingAndRoleNot(String nickname, String role, Pageable pageable);
}