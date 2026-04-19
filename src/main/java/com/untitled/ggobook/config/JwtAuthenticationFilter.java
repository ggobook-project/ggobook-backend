package com.untitled.ggobook.config;

import com.untitled.ggobook.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 클라이언트(리액트)가 보낸 요청 헤더에서 토큰을 꺼냅니다.
        String token = resolveToken(request);

        // 2. 토큰이 존재하고, 위조되지 않은 진짜(유효한) 토큰인지 검사합니다.
        if (token != null && jwtUtil.validateToken(token)) {
            // 3. 진짜 토큰이라면, 토큰 안에서 유저 ID와 권한(Role)을 꺼냅니다.
            String userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            // 4. 스프링 시큐리티 시스템에 "이 사람은 인증된 유저야!"라고 등록합니다. (목에 출입증을 걸어주는 역할)
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.singletonList(authority));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. 문지기의 역할이 끝났으니, 다음 필터나 목적지(Controller)로 통과시켜 줍니다.
        filterChain.doFilter(request, response);
    }

    // 클라이언트가 보낸 "Bearer asdf123..." 형태에서 "Bearer "를 떼어내고 순수 토큰만 남기는 마법의 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer "가 딱 7글자이므로 그 뒤부터 자름
        }
        return null;
    }
}