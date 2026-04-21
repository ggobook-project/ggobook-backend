package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);

    // 중복 확인용 마법의 메서드 3개 추가
    boolean existsByUserId(String userId);     // 아이디가 존재하면 true 반환
    boolean existsByNickname(String nickname); // 닉네임이 존재하면 true 반환
    boolean existsByEmail(String email);       // 이메일이 존재하면 true 반환

    // ==========================================
    //  설계도 기반 조회 로직
    // ==========================================
    Optional<User> findByNameAndEmail(String name, String email); // 아이디 찾기용
    Optional<User> findByEmail(String email);                     // 소셜 로그인 이메일 겹침 방지용

    // 3. [스케줄러용] 현재 시간이 '정지 해제일'을 지났고, 상태가 SUSPENDED인 유저 찾기
    List<User> findByStatusAndSuspensionEndDateBefore(UserStatus status, LocalDateTime now);

    // 1. [아이디 + 닉네임] 둘 중 하나라도 포함되면 검색 (Containing = LIKE %keyword%)
    Page<User> findByUserIdContainingOrNicknameContaining(String userId, String nickname, Pageable pageable);

    // 2. [아이디]로만 검색
    Page<User> findByUserIdContaining(String userId, Pageable pageable);

    // 3. [닉네임]으로만 검색
    Page<User> findByNicknameContaining(String nickname, Pageable pageable);
}