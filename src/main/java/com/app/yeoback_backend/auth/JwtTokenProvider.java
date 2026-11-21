package com.app.yeoback_backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private final UserDetailsService userDetailsService;
    @Value("${jwt.secret}")
    private String secretKey;
    // 로그인 유지 시간 (초). application.yaml 의 jwt.* 로 바꾼다
    @Value("${jwt.access-token-validity-seconds:3600}")
    private long accessTokenValiditySeconds;
    @Value("${jwt.refresh-token-validity-seconds:1209600}")
    private long refreshTokenValiditySeconds;
    private Key key;
    private final HttpServletResponse response;

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    @PostConstruct
    protected void init(){
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    //    액세스 토큰 생성
    public String createAccessToken(String username) {
        final long ACCESS_TOKEN_VALIDITY = accessTokenValiditySeconds * 1000L;
        String accessToken = Jwts.builder()
                .setSubject(username)
                .claim("memberEmail", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        addTokenCookie(ACCESS_TOKEN_COOKIE, accessToken, (int) accessTokenValiditySeconds);

        return accessToken;
    }

    public String createAccessToken(String username, String provider) {
        final long ACCESS_TOKEN_VALIDITY = accessTokenValiditySeconds * 1000L;
        String accessToken = Jwts.builder()
                .setSubject(username)
                .claim("memberEmail", username)
                .claim("provider", provider)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        addTokenCookie(ACCESS_TOKEN_COOKIE, accessToken, (int) accessTokenValiditySeconds);

        return accessToken;
    }

    //    JWT 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
//            ExpiredJwtException: 토큰 만료
//            UnsupportedJwtException: 지원하지 않는 JWT 형식
//            MalformedJwtException: JWT 형식이 잘못되었을 때
//            SignatureException: 서명이 일치하지 않을 때(위조된 토큰)
//            IllegalArgumentException: null, 빈 문자열 등 잘못된 파라미터일 때
            return false;
        }
    }

    //    인증 정보 객체 생성
    public Authentication getAuthentication(String token) {

        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

//        JWT 인증에서는 이미 토큰 자체가 인증 수단이기 때문에 비밀번호를 ""로 전달해도 아무 문제 없다.
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    //    토큰 소유자 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
                .getBody().getSubject();
    }

    //    리프레시 토큰 생성 (서버 저장 없이 쿠키로만 전달)
    public String createRefreshToken(String username){
        final long REFRESH_TOKEN_VALIDITY = refreshTokenValiditySeconds * 1000L;
        String refreshToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        addTokenCookie(REFRESH_TOKEN_COOKIE, refreshToken, (int) refreshTokenValiditySeconds);

        return refreshToken;
    }

    public String createRefreshToken(String username, String provider){
        final long REFRESH_TOKEN_VALIDITY = refreshTokenValiditySeconds * 1000L;
        String refreshToken = Jwts.builder()
                .setSubject(username)
                .claim("provider", provider)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        addTokenCookie(REFRESH_TOKEN_COOKIE, refreshToken, (int) refreshTokenValiditySeconds);

        return refreshToken;
    }

    //    클라이언트 요청에서 액세스 토큰 추출
    public String parseTokenFromHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }

        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    //    클라이언트 요청에서 리프레시 토큰 추출
    public String parseRefreshTokenFromCookie(HttpServletRequest request){
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token).getBody();
    }

    //    로그아웃: 액세스/리프레시 토큰 쿠키 삭제
    public void deleteTokenCookies() {
        addTokenCookie(ACCESS_TOKEN_COOKIE, null, 0);
        addTokenCookie(REFRESH_TOKEN_COOKIE, null, 0);
        log.info("token cookies deleted");
    }

    private void addTokenCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        // 프론트는 localStorage 의 토큰을 쓰므로 쿠키는 JS 에서 읽을 필요가 없다 (XSS 로 탈취 방지)
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if(request.getCookies() != null){
            for(Cookie cookie : request.getCookies()){
                if(name.equals(cookie.getName())){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}