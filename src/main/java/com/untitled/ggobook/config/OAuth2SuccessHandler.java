package com.untitled.ggobook.config;

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

    // 🌟 소셜 로그인이 완벽하게 성공하면 이 메서드가 자동으로 실행됩니다!
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. 어떤 소셜(kakao, naver, google)에서 왔는지 확인합니다.
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        // 2. 소셜 고유 ID를 가져와서 우리 DB에 저장된 형식(예: kakao_12345)으로 조립합니다.
        String providerId = authentication.getName();
        String loginId = provider + "_" + providerId;

        // 3. 유저의 권한(USER)을 꺼냅니다. (앞에 붙은 "ROLE_"은 떼어냅니다)
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        // 🌟 4. 우리가 만든 JwtUtil을 써서 토큰 2개를 뚝딱 만들어냅니다!
        String accessToken = jwtUtil.generateAccessToken(loginId, role);
        String refreshToken = jwtUtil.generateRefreshToken(loginId, role);

        // 5. RefreshToken은 금고(쿠키)에 안전하게 넣습니다. (일반 로그인과 100% 동일한 방식)
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        // 🌟 6. 가장 중요! AccessToken은 리액트 주소 뒤에 파라미터(?token=...)로 달아서 리다이렉트 시킵니다!
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/redirect")
                .queryParam("token", accessToken)
                .build().toUriString();

        // 7. 자, 이제 리액트로 가랏!
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}