package com.app.yeoback_backend.repository;

import com.app.yeoback_backend.dto.MemberDTO;
import com.app.yeoback_backend.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberDAO {
    private final MemberMapper memberMapper;

    // 회원가입
    public void save(MemberDTO memberDTO) {
        memberMapper.insert(memberDTO);
    }
    // 회원 권한 저장
    public void saveRole(MemberDTO memberDTO) {
        memberMapper.insertRole(memberDTO);
    }
    // id로 조회
    public Optional<MemberDTO> findById(Long id) {
        return memberMapper.selectById(id);
    }
    //  이메일 혹은 핸드폰 조회
    public Optional<MemberDTO> findMemberByLoginId(String loginId){
        return memberMapper.selectMemberByLoginId(loginId);
    }
    //  이메일 혹은 핸드폰 가입 여부
    public boolean existsByLoginId(String loginId) {
        return memberMapper.countByLoginId(loginId) > 0;
    }
    // 닉네임이 이미 쓰이는지
    public boolean existsByAlias(String alias) {
        return memberMapper.countByAlias(alias) > 0;
    }
    // 프로필 수정 (닉네임 · 소개)
    public void updateProfile(Long id, String alias, String bio) {
        memberMapper.updateProfile(id, alias, bio);
    }
    // 비밀번호 변경
    public void updatePassword(Long id, String encodedPassword) {
        memberMapper.updatePassword(id, encodedPassword);
    }
}
