package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
}