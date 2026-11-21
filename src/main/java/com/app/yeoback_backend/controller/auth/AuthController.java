package com.app.yeoback_backend.controller.auth;

import com.app.yeoback_backend.auth.CustomUserDetails;
import com.app.yeoback_backend.dto.MemberDTO;
import com.app.yeoback_backend.dto.auth.CodeRequestDTO;
import com.app.yeoback_backend.dto.auth.LoginRequestDTO;
import com.app.yeoback_backend.dto.auth.LoginResponseDTO;
import com.app.yeoback_backend.dto.auth.PasswordResetRequestDTO;
import com.app.yeoback_backend.dto.auth.SentCodeDTO;
import com.app.yeoback_backend.dto.auth.VerifyResultDTO;
import com.app.yeoback_backend.service.AuthService;
import com.app.yeoback_backend.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 인증 API (/api/auth)
 *  POST /login            로그인 → { accessToken, member }
 *  POST /refresh          리프레시 토큰 쿠키 → { accessToken, member } (소셜 로그인 콜백 등)
 *  POST /logout           토큰 쿠키 삭제
 *  GET  /me               현재 로그인 회원 (Authorization: Bearer 필요)
 *  POST /code/send        인증코드 발송 (signup · reset)
 *  POST /code/verify      인증코드 확인
 *  POST /password/reset   비밀번호 재설정
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(HttpServletRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        authService.logout();
        return ResponseEntity.ok(Map.of("message", "로그아웃 되었어요."));
    }

    @GetMapping("/me")
    public ResponseEntity<MemberDTO> me(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(memberService.getMember(user.getLoginId()));
    }

    @PostMapping("/code/send")
    public ResponseEntity<SentCodeDTO> sendCode(@RequestBody CodeRequestDTO request) {
        return ResponseEntity.ok(authService.sendCode(request));
    }

    @PostMapping("/code/verify")
    public ResponseEntity<VerifyResultDTO> verifyCode(@RequestBody CodeRequestDTO request) {
        return ResponseEntity.ok(authService.verifyCode(request));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody PasswordResetRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "비밀번호를 변경했어요."));
    }
}
