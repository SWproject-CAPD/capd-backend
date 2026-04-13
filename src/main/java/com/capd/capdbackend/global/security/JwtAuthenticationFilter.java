package com.capd.capdbackend.global.security;


import com.capd.capdbackend.global.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 토큰 추출 시도
        String token = resolveToken(request);
        log.info("1. 필터 시작 - 요청 URL: {}, 토큰 존재 여부: {}", request.getRequestURI(), (token != null));

        try {
            // 토큰 유효성 검사
            if (token != null && jwtProvider.validateToken(token)) {

                // 이메일 추출
                String email = jwtProvider.extractSocialId(token);
                log.info("2. 토큰 검증 성공 - 추출된 이메일: {}", email);

                // DB 조회
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                log.info("3. DB 유저 조회 성공 - 권한: {}", userDetails.getAuthorities());

                // 인증 객체 생성 및 저장
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("4. SecurityContext 저장 완료! (인증 성공)");
            } else {
                log.warn("토큰이 없거나 validateToken이 false를 반환함.");
            }
        } catch (Exception e) {
            log.error("필터 실행 중 에러 발생! 원인: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 로그를 찍어서 헤더에 뭐가 들어오는지 확인
        log.info("Header Authorization 값: {}", bearerToken);

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
