package com.app.yeoback_backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 *  1. Authorization: Bearer <accessToken> (없으면 accessToken 쿠키) 가 유효하면 인증
 *  2. 액세스 토큰이 없거나 만료/위조면 refreshToken 쿠키로 새 액세스 토큰을 발급하고
 *     응답 헤더 Authorization 에 담는다 → 프론트(ajax.jsx)가 받아서 localStorage 를 갱신
 *  3. 토큰의 회원이 탈퇴/비활성 등으로 조회되지 않으면 인증 없이 다음 필터로 넘긴다
 *     (보호된 API 는 AuthenticationHandler 가 401 로 응답)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtTokenProvider.parseTokenFromHeader(request);

        try {
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                authenticate(jwtTokenProvider.getAuthentication(accessToken), request);
            } else {
                if (accessToken != null) log.info("Access token is not valid, try refresh token");
                refresh(request, response);
            }
        } catch (UsernameNotFoundException e) {
            log.info("Skip authentication restore: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

//        이 코드를 호출해서 필터 체인에 있는 다음 필터에게 요청과 응답 처리를 넘김
//        만약 doFilter 메소드를 호출하지 않으면, 필터 체인의 다음 필터는 실행되지 않고 요청이 멈춤
        filterChain.doFilter(request, response);
    }

    //    쿠키의 리프레시 토큰으로 재발급
    private void refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenProvider.parseRefreshTokenFromCookie(request);
        if (refreshToken == null) return;
//        유효성 검증을 먼저 해야 만료/위조된 토큰에서 getUsername()이 예외를 던지지 않는다
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.info("Refresh token is not valid");
            return;
        }
        String username = jwtTokenProvider.getUsername(refreshToken);
        authenticate(jwtTokenProvider.getAuthentication(refreshToken), request);

        String newAccessToken = jwtTokenProvider.createAccessToken(username);
        jwtTokenProvider.createRefreshToken(username);
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        log.info("Access token reissued by refresh token");
    }

    private void authenticate(Authentication authentication, HttpServletRequest request) {
        if (authentication instanceof AbstractAuthenticationToken token) {
//            요청 정보 저장
            token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
