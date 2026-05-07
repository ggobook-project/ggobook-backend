package com.untitled.ggobook.config;

import com.untitled.ggobook.domain.MemberSuspend;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.domain.enums.UserStatus;
import com.untitled.ggobook.repository.MemberSuspendRepository;
import com.untitled.ggobook.repository.UserRepository;
import com.untitled.ggobook.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final MemberSuspendRepository memberSuspendRepository; // 🌟 정지 사유 조회를 위해 추가

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String providerId = authentication.getName();
        String loginId = provider + "_" + providerId;

        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new RuntimeException("소셜 유저를 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            LocalDateTime now = LocalDateTime.now();
            if (user.getSuspensionEndDate() != null && user.getSuspensionEndDate().isBefore(now)) {
                user.release();
                userRepository.save(user);
            } else {
                MemberSuspend activeSuspend = memberSuspendRepository.findFirstByUserIdAndEndDateAfterOrderByEndDateDesc(user.getId(), now).orElse(null);
                String reason = (activeSuspend != null) ? activeSuspend.getReason() : "관리자 규정 위반";
                String endDate = user.getSuspensionEndDate() != null ? user.getSuspensionEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "영구 정지";
                String encodedReason = URLEncoder.encode(reason, StandardCharsets.UTF_8);
                String encodedDate = URLEncoder.encode(endDate, StandardCharsets.UTF_8);

                String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/login")
                        .queryParam("error", "SUSPENDED")
                        .queryParam("reason", encodedReason)
                        .queryParam("date", encodedDate)
                        .build(true).toUriString();

                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }
        }

        if (user.getStatus() == UserStatus.WITHDRAWN) {
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/login")
                    .queryParam("error", "WITHDRAWN")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String accessToken = jwtUtil.generateAccessToken(user.getId(), role);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), role);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(14 * 24 * 60 * 60)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/redirect")
                .queryParam("token", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}