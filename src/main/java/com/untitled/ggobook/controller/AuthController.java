package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.LoginRequest;
import com.untitled.ggobook.domain.SignupRequest;
import com.untitled.ggobook.entity.User;
import com.untitled.ggobook.service.AuthService;
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
    private final JwtUtil jwtUtil; //  토큰 발급기

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
    // 2. 진짜 회원가입 & 로그인 & 로그아웃 API
    // ==========================================


    // 🌟 4. 회원가입 (검문소 설치 버전)
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequest request, // 🌟 @Valid: DTO의 경고 딱지들을 작동시켜라!
            BindingResult bindingResult // 🌟 BindingResult: 검사에서 걸린 에러들을 모아두는 바구니
    ) {
        // 1. 에러 바구니에 에러가 하나라도 들어있다면?
        if (bindingResult.hasErrors()) {
            // 가장 첫 번째로 걸린 에러 메시지(예: "아이디를 입력해주세요.")를 꺼냅니다.
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();

            // 프론트엔드에게 400 Bad Request 에러와 함께 메시지를 던져서 쫓아냅니다.
            return ResponseEntity.badRequest().body(errorMessage);
        }

        // 2. 검문을 무사히 통과했다면 원래대로 공장장에게 가입을 맡깁니다.
        authService.signup(request);
        return ResponseEntity.ok("회원가입 성공!");
    }

    // 5. 로그인 (검증 후 토큰 2개 발급)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        // 1. 공장장에게 비번 검증 맡기기
        User user = authService.login(request.getUserId(), request.getPassword());

        // 2. 검증 통과하면 토큰 2개 만들기
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getRole());

        // 3. Refresh Token은 안전하게 금고(쿠키)에 넣기
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일 (초 단위)
        response.addCookie(cookie);

        // 4. Access Token은 프론트엔드에 바로 던져주기
        return ResponseEntity.ok(accessToken);
    }

    // 6. 로그아웃 (쿠키 삭제)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // 똑같은 이름의 빈 쿠키를 만들고, 수명을 0초로 설정해서 덮어씌웁니다 (삭제 효과)
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("로그아웃 성공!");
    }
}