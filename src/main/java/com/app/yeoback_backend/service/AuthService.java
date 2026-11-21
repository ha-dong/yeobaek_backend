package com.app.yeoback_backend.service;

import com.app.yeoback_backend.auth.CustomUserDetails;
import com.app.yeoback_backend.auth.JwtTokenProvider;
import com.app.yeoback_backend.common.enumeration.CodePurpose;
import com.app.yeoback_backend.common.enumeration.ContactMethod;
import com.app.yeoback_backend.common.exception.ApiException;
import com.app.yeoback_backend.common.exception.MemberLoginFailException;
import com.app.yeoback_backend.dto.MemberDTO;
import com.app.yeoback_backend.dto.auth.CodeRequestDTO;
import com.app.yeoback_backend.dto.auth.LoginRequestDTO;
import com.app.yeoback_backend.dto.auth.LoginResponseDTO;
import com.app.yeoback_backend.dto.auth.PasswordResetRequestDTO;
import com.app.yeoback_backend.dto.auth.SentCodeDTO;
import com.app.yeoback_backend.dto.auth.VerifyResultDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final VerificationService verificationService;

    /**
     * 로그인
     *  1. loginId 정규화 (이메일 소문자 / 휴대폰 숫자만)
     *  2. AuthenticationManager → CustomUserDetailService.loadUserByUsername → BCrypt 비밀번호 비교
     *  3. 성공하면 access/refresh JWT 발급, 회원 정보와 함께 반환
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        if (request.getLoginId() == null || request.getMemberPassword() == null) {
            throw new MemberLoginFailException("아이디 또는 비밀번호가 비어 있음");
        }
        String loginId = ContactMethod.guess(request.getLoginId()).normalize(request.getLoginId());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginId, request.getMemberPassword()));
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId());
        jwtTokenProvider.createRefreshToken(user.getLoginId());

        MemberDTO member = memberService.getMember(user.getLoginId());
        log.info("login success: id={}", member.getId());
        return new LoginResponseDTO(accessToken, member);
    }

    /**
     * 리프레시 토큰 쿠키로 새 액세스 토큰과 회원 정보를 받는다
     *  - 소셜 로그인 콜백 화면이 토큰을 localStorage 에 담을 때 사용
     *  - 리프레시 토큰도 새로 발급해 유지 시간을 다시 시작한다
     */
    public LoginResponseDTO refresh(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.parseRefreshTokenFromCookie(request);
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "no_session", "로그인이 필요해요.");
        }
        String loginId = jwtTokenProvider.getUsername(refreshToken);
        MemberDTO member = memberService.findActiveMember(loginId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no_session", "로그인이 필요해요."));

        String accessToken = jwtTokenProvider.createAccessToken(loginId);
        jwtTokenProvider.createRefreshToken(loginId);
        return new LoginResponseDTO(accessToken, member);
    }

    public void logout() {
        jwtTokenProvider.deleteTokenCookies();
    }

    /** 인증코드 발송: 가입이면 중복 계정 거절, 재설정이면 없는 계정 거절 */
    public SentCodeDTO sendCode(CodeRequestDTO request) {
        CodePurpose purpose = required(request.getPurpose(), "purpose");
        ContactMethod method = required(request.getMethod(), "method");
        String contact = method.normalize(request.getContact());
        if (!method.isValid(contact)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", method == ContactMethod.EMAIL ? "이메일 형식이 올바르지 않아요." : "휴대폰 번호 형식이 올바르지 않아요.");
        }

        boolean exists = memberService.exists(contact);
        if (purpose == CodePurpose.SIGNUP && exists) {
            throw new ApiException(HttpStatus.CONFLICT, "duplicate", "이미 가입된 계정이에요.");
        }
        if (purpose == CodePurpose.RESET && !exists) {
            throw new ApiException(HttpStatus.NOT_FOUND, "not_found", "가입된 계정을 찾지 못했어요.");
        }
        return verificationService.send(purpose, method, contact);
    }

    public VerifyResultDTO verifyCode(CodeRequestDTO request) {
        CodePurpose purpose = required(request.getPurpose(), "purpose");
        ContactMethod method = required(request.getMethod(), "method");
        return verificationService.verify(purpose, method.normalize(request.getContact()), request.getCode());
    }

    /** 비밀번호 재설정: reset 용도 인증을 마친 경우에만 */
    public void resetPassword(PasswordResetRequestDTO request) {
        ContactMethod method = required(request.getMethod(), "method");
        String contact = method.normalize(request.getContact());
        if (!verificationService.isVerified(CodePurpose.RESET, contact)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "not_verified", "인증코드 확인을 먼저 진행해 주세요.");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "새 비밀번호를 입력해 주세요.");
        }
        memberService.changePassword(contact, request.getNewPassword());
        verificationService.consume(CodePurpose.RESET, contact);
    }

    private <T> T required(T value, String name) {
        if (value == null) throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", name + " 값이 올바르지 않아요.");
        return value;
    }
}
