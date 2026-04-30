package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.MemberSuspend;
import com.untitled.ggobook.domain.SignupRequest;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.Wallet;
import com.untitled.ggobook.domain.enums.UserStatus;
import com.untitled.ggobook.repository.MemberSuspendRepository;
import com.untitled.ggobook.repository.UserRepository;
import com.untitled.ggobook.repository.WalletRepository;
import com.untitled.ggobook.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final WalletRepository walletRepository;

    // 정지 사유 장부를 읽기 위해 요원을 투입합니다.
    private final MemberSuspendRepository memberSuspendRepository;

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

    // 4. 회원가입 로직
    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByUserId(request.getUserId()) ||
                userRepository.existsByEmail(request.getEmail()) ||
                userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 사용중인 정보가 포함되어 있습니다.");
        }

        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setRole("USER");
        user.setGender(request.getGender());

        userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setBalance(0);
        wallet.setUser(user);

        walletRepository.save(wallet);
    }

    // 5. 로그인 (정지/탈퇴 검문소 추가됨)
    @Transactional
    public User login(String userId, String password) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        // 🚨 1단계: 탈퇴 회원 입구 컷
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new RuntimeException("탈퇴 처리된 계정입니다. 고객센터에 문의해주세요.");
        }

        // 🚨 2단계: 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 🚨 3단계: 정지 회원 검문소
        if (user.getStatus() == UserStatus.SUSPENDED) {
            LocalDateTime now = LocalDateTime.now();

            // 정지 기간이 이미 종료되었는지 확인
            if (user.getSuspensionEndDate() != null && user.getSuspensionEndDate().isBefore(now)) {
                // [자동 해제] 기간 지났으면 정상으로 복구!
                user.release();
            } else {
                // [차단 진행] 아직 정지 중이면 장부에서 최근 사유를 꺼내옵니다.
                MemberSuspend activeSuspend = memberSuspendRepository.findActiveSuspend(user.getId(), now)
                        .orElse(null);

                String reason = (activeSuspend != null) ? activeSuspend.getReason() : "관리자 규정 위반";
                String endDate = user.getSuspensionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                throw new RuntimeException("SUSPENDED|" + reason + "|" + endDate);
            }
        }

        return user;
    }

    // 🌟 핵심 수술 5-1: Refresh Token을 Redis에 보관 (15시간 -> 14일로 연장!)
    public void saveRefreshToken(Long userId, String refreshToken) {
        // 프론트엔드 쿠키 수명(14일)과 일치시켜서 중간에 튕기는 현상을 방지합니다.
        redisTemplate.opsForValue().set("RT:" + userId, refreshToken, Duration.ofDays(14));
    }

    // 5-2. Access Token 재발급 로직
    @Transactional(readOnly = true)
    public String refreshAccessToken(String refreshToken, JwtUtil jwtUtil) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("만료되었거나 유효하지 않은 리프레시 토큰입니다. 다시 로그인해주세요.");
        }

        Long userId = jwtUtil.getIdFromToken(refreshToken);
        String role = jwtUtil.getRoleFromToken(refreshToken);

        String storedToken = redisTemplate.opsForValue().get("RT:" + userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new RuntimeException("보안 문제로 로그아웃 처리되었습니다. 다시 로그인해주세요.");
        }

        return jwtUtil.generateAccessToken(userId, role);
    }

    // 로그아웃 시 Redis 토큰 파기
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("RT:" + userId);
    }

    // 6. 아이디 찾기
    @Transactional(readOnly = true)
    public String findId(String name, String email) {
        User user = userRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new RuntimeException("일치하는 회원 정보가 없습니다."));

        String userId = user.getUserId();
        emailService.sendFullIdEmail(email, userId);

        if (userId.length() > 3) {
            return userId.substring(0, userId.length() - 3) + "***";
        }
        return userId;
    }

    // 7. 비밀번호 재설정 링크 생성 발송
    @Transactional(readOnly = true)
    public void generateAndSendResetToken(String userId, String name, String email) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("일치하는 회원 정보가 없습니다."));

        if (!user.getName().equals(name) || !user.getEmail().equals(email)) {
            throw new RuntimeException("회원 정보가 일치하지 않습니다.");
        }

        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("RESET:" + resetToken, user.getUserId(), Duration.ofMinutes(30));
        emailService.sendPasswordResetLink(email, resetToken);
    }

    // 8. 비밀번호 재설정
    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        String userId = redisTemplate.opsForValue().get("RESET:" + token);
        if (userId == null) {
            throw new RuntimeException("유효하지 않거나 만료된 링크입니다. 다시 시도해 주세요.");
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        user.setPassword(passwordEncoder.encode(newPassword));
        redisTemplate.delete("RESET:" + token);
    }
}