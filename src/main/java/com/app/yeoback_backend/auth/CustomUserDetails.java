package com.app.yeoback_backend.auth;

import com.app.yeoback_backend.common.enumeration.MemberRole;
import com.app.yeoback_backend.common.enumeration.Status;
import com.app.yeoback_backend.dto.MemberDTO;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@ToString
public class CustomUserDetails implements UserDetails {
    private Long id;
    private String memberEmail;
    private String memberPhone;
    private String memberPassword;
    private String memberName;
    private String memberAlias;
    private String memberBio;
    private Status memberStatus;
    private MemberRole memberRole;
    private String loginId;         // 로그인한 이메일 혹은 전화번호

    public CustomUserDetails(MemberDTO memberDTO, String loginId) {
        this.id = memberDTO.getId();
        this.memberEmail = memberDTO.getMemberEmail();
        this.memberPhone = memberDTO.getMemberPhone();
        this.memberPassword = memberDTO.getMemberPassword();
        this.memberAlias = memberDTO.getMemberAlias();
        this.memberName = memberDTO.getMemberName();
        this.memberBio = memberDTO.getMemberBio();
        this.memberStatus = memberDTO.getMemberStatus();
        this.memberRole = memberDTO.getMemberRole();
        this.loginId = loginId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // tbl_auth 행이 없는 회원(권한 저장 로직 추가 전 가입)은 일반 회원으로 본다
        return (memberRole == null ? MemberRole.MEMBER : memberRole).getAuthorities();
    }

    @Override
    public String getPassword() {
        return memberPassword;
    }

    @Override
    public String getUsername() {
        return loginId;
    }
}
