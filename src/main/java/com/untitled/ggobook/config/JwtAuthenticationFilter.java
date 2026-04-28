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
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 로그인, 회원가입, 이메일, 공개 컨텐츠 API는 토큰 검사 생략
        return path.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            // 1. 토큰이 유효한 경우 (정상 통과)
            if (jwtUtil.validateToken(token)) {
                Long id = jwtUtil.getIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(id, null, Collections.singletonList(authority));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // ==========================================
            // 🌟 핵심 수술 부위: 토큰이 썩었으면 401(Unauthorized) 에러를 프론트 요원에게 투척!
            // ==========================================
            else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 에러 코드 세팅
                response.getWriter().write("Access Token Expired"); // 에러 메시지
                return; // 더 이상 컨트롤러로 못 들어가게 여기서 필터 체인 종료 (입구컷)
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}