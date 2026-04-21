package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.LoginRequest;
import com.untitled.ggobook.domain.SignupRequest;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.service.AuthService;
import com.untitled.ggobook.service.EmailService;
import com.untitled.ggobook.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil; // 토큰 발급기
    private final EmailService emailService;

    // ==========================================
    // 1. 중복 확인 API
    // ==========================================
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkIdDuplicate(@RequestParam String userId) {
        return ResponseEntity.ok(authService.checkIdDuplicate(userId));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNicknameDuplicate(@RequestParam String nickname) {
        return ResponseEntity.ok(authService.checkNicknameDuplicate(nickname));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email) {
        return ResponseEntity.ok(authService.checkEmailDuplicate(email));
    }

    // ==========================================
    // 🌟 2. 이메일 인증 API (회색 글씨 해결!)
    // ==========================================

    // 이메일 발송 창구 (sendAuthEmail 뼈대 연결)
    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(@RequestParam String email) {
        emailService.sendAuthEmail(email);
        return ResponseEntity.ok("이메일 발송 성공");
    }

    // 이메일 인증번호 확인 창구 (verifyAuthCode 뼈대 연결)
    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String code) {
        boolean isVerified = emailService.verifyAuthCode(email, code);

        if (isVerified) {
            return ResponseEntity.ok("인증 성공");
        } else {
            // 번호가 틀렸거나 3분이 지나면 400 에러를 프론트로 던집니다.
            return ResponseEntity.badRequest().body("인증번호가 틀렸거나 만료되었습니다.");
        }
    }

    // ==========================================
    // 3. 진짜 회원가입 & 로그인 & 로그아웃 API
    // ==========================================

    // 4. 회원가입 (검문소 설치 버전)
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMessage);
        }

        authService.signup(request);
        return ResponseEntity.ok("회원가입 성공!");
    }

    // 5. 로그인 (검증 후 토큰 2개 발급)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        User user = authService.login(request.getUserId(), request.getPassword());

        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getRole());

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok(accessToken);
    }

    // 6. 로그아웃 (쿠키 삭제)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("로그아웃 성공!");
    }
}