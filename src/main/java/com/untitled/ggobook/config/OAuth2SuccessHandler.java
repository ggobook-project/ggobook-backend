package com.untitled.ggobook.config;

import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.repository.UserRepository;
import com.untitled.ggobook.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository; // 추가: 유저 PK를 찾기 위해 필요

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String providerId = authentication.getName();
        String loginId = provider + "_" + providerId;

        // DB에서 소셜 로그인 유저를 찾아 PK(id)를 확보합니다.
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new RuntimeException("소셜 유저를 찾을 수 없습니다."));

        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        // 🌟 수정됨: loginId 대신 user.getId() 전달
        String accessToken = jwtUtil.generateAccessToken(user.getId(), role);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), role);

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/redirect")
                .queryParam("token", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}