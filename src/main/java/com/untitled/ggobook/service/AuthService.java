package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.SignupRequest;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

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
    //  6 아이디 찾기 로직 (마스킹 응답 + 전체 아이디 이메일 동시 발송)
    @Transactional(readOnly = true)
    public String findId(String name, String email) {
        User user = userRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new RuntimeException("일치하는 회원 정보가 없습니다."));

        String userId = user.getUserId();

        // 1. 유저 몰래 전체 아이디를 이메일로 슝! 발송합니다.
        emailService.sendFullIdEmail(email, userId);

        // 2. 리액트 화면에 보여줄 아이디는 뒤에 3자리를 마스킹 처리합니다.
        if (userId.length() > 3) {
            return userId.substring(0, userId.length() - 3) + "***";
        }
        return userId;
    }

    // 7. 비밀번호 재설정 링크 생성 및 발송 로직 (30분 유효기간)
    @Transactional(readOnly = true)
    public void generateAndSendResetToken(String userId, String name, String email) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("일치하는 회원 정보가 없습니다."));

        if (!user.getName().equals(name) || !user.getEmail().equals(email)) {
            throw new RuntimeException("회원 정보가 일치하지 않습니다.");
        }

        // 1회용 특수 마스터키(토큰) 생성
        String resetToken = UUID.randomUUID().toString();

        // Redis에 저장 (Key: RESET:토큰값, Value: 유저아이디, 시간: 30분)
        redisTemplate.opsForValue().set("RESET:" + resetToken, user.getUserId(), Duration.ofMinutes(30));

        // 이메일 발송
        emailService.sendPasswordResetLink(email, resetToken);
    }

    // 8. 이메일 링크를 통한 실제 비밀번호 변경 로직
    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        // 1. Redis에서 토큰으로 유저 아이디 꺼내기
        String userId = redisTemplate.opsForValue().get("RESET:" + token);
        if (userId == null) {
            throw new RuntimeException("유효하지 않거나 만료된 링크입니다. 다시 시도해 주세요.");
        }

        // 2. 유저 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        // 3. 새 비밀번호 암호화 후 DB에 덮어쓰기
        user.setPassword(passwordEncoder.encode(newPassword));

        // 4. 보안을 위해 한 번 사용된 토큰은 Redis에서 즉시 파기 (1회용 보장!)
        redisTemplate.delete("RESET:" + token);
    }

}