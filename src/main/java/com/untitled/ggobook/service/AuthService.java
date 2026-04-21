package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.SignupRequest;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. 아이디 중복 확인
    @Transactional(readOnly = true)
    public boolean checkIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }

    // 2. 닉네임 중복 확인
    @Transactional(readOnly = true)
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // 3. 이메일 중복 확인
    @Transactional(readOnly = true)
    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    // 4.회원가입 로직 (최종 검증 + DB 저장)
    @Transactional
    public void signup(SignupRequest request) {
        // 프론트에서 중복검사를 하고 왔겠지만, 백엔드에서 마지막으로 한 번 더 철저하게 막아줍니다. (현업 이중 검증 원칙)
        if (userRepository.existsByUserId(request.getUserId()) ||
                userRepository.existsByEmail(request.getEmail()) ||
                userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 사용중인 정보가 포함되어 있습니다.");
        }

        // 새 유저 생성 및 암호화
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt 암호화
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setRole("USER"); // 기본 권한
        user.setGender(request.getGender());

        userRepository.save(user); // DB에 쏙!
    }

    // 5. 로그인 검증 로직
    @Transactional(readOnly = true)
    public User login(String userId, String password) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        // 평문 비번과 DB의 암호화된 비번 비교
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user; // 검증 통과하면 유저 정보 반환
    }
}