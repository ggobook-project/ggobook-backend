package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.LoginRequestDto;
import com.untitled.ggobook.dto.SignupRequestDto;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.service.AuthService;
import com.untitled.ggobook.service.EmailService;
import com.untitled.ggobook.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
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

    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(@RequestParam String email) {
        emailService.sendAuthEmail(email);
        return ResponseEntity.ok("이메일 발송 성공");
    }

    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String code) {
        boolean isVerified = emailService.verifyAuthCode(email, code);

        if (isVerified) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.badRequest().body("인증번호가 틀렸거나 만료되었습니다.");
        }
    }

    // ==========================================
    // 3. 진짜 회원가입 & 로그인 & 로그아웃 API
    // ==========================================

    // 4. 회원가입 (검문소 설치 버전)
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequestDto request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMessage);
        }

        authService.signup(request);
        return ResponseEntity.ok("회원가입 성공!");
    }

    // 🌟 5. 로그인 (AuthService의 Redis 로직 적용 + ResponseCookie 적용)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        User user = authService.login(request.getUserId(), request.getPassword());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getRole());

        // Redis에 Refresh Token 안전하게 보관!
        authService.saveRefreshToken(user.getId(), refreshToken);

        // 🌟 핵심 수술 부위: 체크박스 상태에 따라 쿠키 수명 설정
        long maxAgeSeconds = request.isKeepLoggedIn() ? (14 * 24 * 60 * 60) : -1;

        // 🌟 브라우저 차단 정책을 뚫는 안전한 쿠키 생성 (SameSite=Lax 적용)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax") // 크롬 등 최신 브라우저 보안 정책 대응
                .maxAge(maxAgeSeconds)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(accessToken);
    }

    // 🌟 5-1. 대기업 무한 로그인 비밀 API (Access Token 재발급)
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("리프레시 토큰이 없습니다. 다시 로그인해주세요.");
        }

        String newAccessToken = authService.refreshAccessToken(refreshToken, jwtUtil);
        return ResponseEntity.ok(newAccessToken);
    }

    // 🌟 6. 로그아웃 (쿠키 삭제 + Redis 파기)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            Long userId = jwtUtil.getIdFromToken(refreshToken);
            authService.deleteRefreshToken(userId);
        }

        // 🌟 로그아웃 시에도 ResponseCookie로 깔끔하게 파기
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0) // 즉시 삭제
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok("로그아웃 성공!");
    }

    // 7. 아이디 찾기 API
    @GetMapping("/find-id")
    public ResponseEntity<String> findId(@RequestParam String name, @RequestParam String email) {
        String maskedId = authService.findId(name, email);
        return ResponseEntity.ok(maskedId);
    }

    // 8. 비밀번호 재설정 메일 발송 API
    @PostMapping("/password/reset-link")
    public ResponseEntity<String> requestPasswordReset(
            @RequestParam String userId,
            @RequestParam String name,
            @RequestParam String email) {

        authService.generateAndSendResetToken(userId, name, email);
        return ResponseEntity.ok("가입된 이메일로 비밀번호 재설정 링크가 발송되었습니다.");
    }

    // 9. 실제 비밀번호 변경 API
    @PostMapping("/password/reset")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        authService.resetPasswordWithToken(token, newPassword);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    // 예외 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}