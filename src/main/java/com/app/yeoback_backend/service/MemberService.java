package com.app.yeoback_backend.service;

import com.app.yeoback_backend.aop.annotation.LogStatus;
import com.app.yeoback_backend.aop.annotation.LogStatusWithReturn;
import com.app.yeoback_backend.common.enumeration.CodePurpose;
import com.app.yeoback_backend.common.enumeration.ContactMethod;
import com.app.yeoback_backend.common.enumeration.MemberRole;
import com.app.yeoback_backend.common.exception.ApiException;
import com.app.yeoback_backend.common.exception.MemberNotFoundException;
import com.app.yeoback_backend.dto.MemberDTO;
import com.app.yeoback_backend.repository.MemberDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberDAO memberDAO;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final MemberDefaultsService memberDefaultsService;

    /**
     * 회원가입
     *  1. 이메일/휴대폰 정규화 (프론트와 같은 규칙)
     *  2. 인증코드 확인을 마쳤는지 검사
     *  3. 중복 검사
     *  4. tbl_member insert → 생성된 id 로 tbl_auth(member_role = member) insert
     *  5. 성공하면 인증 완료 상태 소모
     */
    @Transactional
    @LogStatus
    public void join(MemberDTO memberDTO) {
        boolean byEmail = memberDTO.getMemberEmail() != null && !memberDTO.getMemberEmail().isBlank();
        ContactMethod method = byEmail ? ContactMethod.EMAIL : ContactMethod.PHONE;
        String contact = method.normalize(byEmail ? memberDTO.getMemberEmail() : memberDTO.getMemberPhone());
        if (!method.isValid(contact)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", method == ContactMethod.EMAIL ? "이메일 형식이 올바르지 않아요." : "휴대폰 번호 형식이 올바르지 않아요.");
        }
        if (memberDTO.getMemberPassword() == null || memberDTO.getMemberPassword().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "비밀번호를 입력해 주세요.");
        }
        memberDTO.setMemberEmail(byEmail ? contact : null);
        memberDTO.setMemberPhone(byEmail ? null : contact);

        if (!verificationService.isVerified(CodePurpose.SIGNUP, contact)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "not_verified", "인증코드 확인을 먼저 진행해 주세요.");
        }
        if (memberDAO.existsByLoginId(contact)) {
            throw new ApiException(HttpStatus.CONFLICT, "duplicate", "이미 가입된 계정이에요.");
        }

        memberDTO.setMemberPassword(passwordEncoder.encode(memberDTO.getMemberPassword()));
        memberDTO.setMemberAlias(memberDefaultsService.newNickname());
        memberDAO.save(memberDTO);

        memberDTO.setMemberRole(MemberRole.MEMBER);
        memberDAO.saveRole(memberDTO);
        // 기본 설정 행 (공개 설정 · 컬러 등)
        memberDefaultsService.createDefaultSetting(memberDTO.getId());

        verificationService.consume(CodePurpose.SIGNUP, contact);
    }

    //  프로필 수정 텍스트 저장
    @Transactional
    @LogStatus
    public void update(MemberDTO memberDTO) {
        // 이름(닉네임), 자기소개, 생년월일만 갱신한다.
        // 이미지 교체 흐름은 컨트롤러에서 upload/save/delete 순서로 직접 조합한다.
//        memberDAO.update(memberDTO);
    }

    //    회원정보 조회
    @LogStatusWithReturn
    public MemberDTO getMember(String loginId){
        return memberDAO.findMemberByLoginId(loginId).orElseThrow(MemberNotFoundException::new);
    }

    //    활동 중인 회원 조회 (없으면 empty)
    public java.util.Optional<MemberDTO> findActiveMember(String loginId) {
        return memberDAO.findMemberByLoginId(loginId);
    }

    //    가입 여부 (상태 무관)
    public boolean exists(String loginId) {
        return memberDAO.existsByLoginId(loginId);
    }

    //    비밀번호 변경 (인증코드 확인 후)
    @Transactional
    @LogStatus
    public void changePassword(String loginId, String rawPassword) {
        MemberDTO member = getMember(loginId);
        if (passwordEncoder.matches(rawPassword, member.getMemberPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "same_as_before", "이전에 사용한 비밀번호예요.");
        }
        memberDAO.updatePassword(member.getId(), passwordEncoder.encode(rawPassword));
    }
}
