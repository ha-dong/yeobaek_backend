package com.app.yeoback_backend.controller.member;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 화면은 React(yeoback_front)가 그린다.
 * 이 컨트롤러는 서버 쪽에서 "/member/**" 로 보내는 요청(OAuth 성공/실패, 인증 실패 등)을
 * 같은 경로의 React 화면으로 넘겨주는 역할만 한다.
 *   /member/join  → React 회원가입 화면 (쿼리스트링·해시 그대로 전달)
 *   /member/login → React 로그인 화면
 */
@Controller
@RequestMapping("/member")
@Slf4j
public class MemberController {

    // application.yaml 의 app.front-url (기본값: Vite 개발 서버)
    @Value("${app.front-url:http://localhost:5173}")
    private String frontUrl;

    // 회원가입 페이지 이동
    @GetMapping("/join")
    public RedirectView goToJoinPage(HttpServletRequest request) {
        return toFront("/member/join", request.getQueryString(), null);
    }

    // 로그인 페이지 이동
    @GetMapping("/login")
    public RedirectView login(@CookieValue(value = "remember", required = false) boolean remember,
                              @CookieValue(value = "rememberEmail", required = false) String rememberEmail,
                              HttpServletRequest request) {
        // 쿠키는 백엔드 도메인에 있어서 React가 직접 못 읽으므로 쿼리로 넘긴다
        String email = remember ? rememberEmail : null;
        return toFront("/member/login", request.getQueryString(), email);
    }

    private RedirectView toFront(String path, String queryString, String rememberEmail) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(frontUrl).path(path).query(queryString);
        if (rememberEmail != null && !rememberEmail.isBlank()) {
            builder.queryParam("rememberEmail", rememberEmail);
        }
        String target = builder.build().encode().toUriString();
        log.info("redirect to front: {}", target);
        return new RedirectView(target);
    }
}
