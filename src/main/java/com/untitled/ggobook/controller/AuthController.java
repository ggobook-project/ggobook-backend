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

        // 🌟 수정됨: user.getUserId() 대신 user.getId() 파라미터 2개만 전달
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getRole());

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

    // 7. 아이디 찾기 API
    @GetMapping("/find-id")
    public ResponseEntity<String> findId(@RequestParam String name, @RequestParam String email) {
        String maskedId = authService.findId(name, email);
        return ResponseEntity.ok(maskedId);
    }

    // 8. 비밀번호 재설정 메일 발송 API (기존 비번 찾기 창에서 호출)
    @PostMapping("/password/reset-link")
    public ResponseEntity<String> requestPasswordReset(
            @RequestParam String userId,
            @RequestParam String name,
            @RequestParam String email) {

        authService.generateAndSendResetToken(userId, name, email);
        return ResponseEntity.ok("가입된 이메일로 비밀번호 재설정 링크가 발송되었습니다.");
    }

    // 9. 실제 비밀번호 변경 API (이메일 링크 타고 들어간 화면에서 호출)
    @PostMapping("/password/reset")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        authService.resetPasswordWithToken(token, newPassword);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
    //  추가: 에러 택배 상자 해체 전담 직원 (Clean Code 예외 처리)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        // 백엔드 서비스(AuthService)에서 던진 에러 메시지만 쏙 뽑아서 순수 문자열(String)로 반환합니다.
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}