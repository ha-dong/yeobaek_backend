package com.app.yeoback_backend.auth;

import com.app.yeoback_backend.service.OAuthLoginService;
import com.app.yeoback_backend.service.OAuthLoginService.OAuthLoginException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 소셜 로그인 성공(공급자 인증 완료) 후 처리
 *  - OAuthLoginService 로 회원을 찾거나 연동하거나 가입시킨다
 *  - 액세스/리프레시 토큰을 HttpOnly 쿠키로 내려주고 React 의 /oauth/callback 으로 보낸다
 *    (토큰을 URL 에 싣지 않는다. 콜백 화면이 POST /api/auth/refresh 로 토큰과 회원 정보를 받아 간다)
 *  - 진행할 수 없으면 React 로그인 화면으로 보내며 ?oauthError=<reason> 을 붙인다
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthLoginService oAuthLoginService;

    @Value("${app.front-url:http://localhost:5173}")
    private String frontUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String provider = oAuth2User.getAttribute("provider");

        try {
            String loginId = oAuthLoginService.login(
                    provider,
                    attr(oAuth2User, "id"),
                    oAuth2User.getAttribute("email"),
                    oAuth2User.getAttribute("name"),
                    oAuth2User.getAttribute("profile"));

            jwtTokenProvider.createAccessToken(loginId, provider);
            jwtTokenProvider.createRefreshToken(loginId, provider);
            response.sendRedirect(frontUrl + "/oauth/callback");
        } catch (OAuthLoginException e) {
            log.warn("oauth login stopped ({}): {}", e.getReason(), e.getMessage());
            response.sendRedirect(frontUrl + "/member/login?oauthError=" + e.getReason());
        }
    }

    private String attr(OAuth2User user, String name) {
        Object value = user.getAttribute(name);
        return value == null ? null : value.toString();
    }
}
