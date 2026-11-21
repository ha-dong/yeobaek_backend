package com.app.yeoback_backend.mapper;

import com.app.yeoback_backend.dto.MemberDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface MemberMapper {
    // 회원가입
    void insert(MemberDTO memberDTO);
    // 회원 권한 저장 (id, memberRole 사용)
    void insertRole(MemberDTO memberDTO);
    // id로 조회
    Optional<MemberDTO> selectById(Long id);
    // 이메일 혹은 핸드폰 조회 (활동 중인 회원)
    Optional<MemberDTO> selectMemberByLoginId(String loginId);
    // 이메일 혹은 핸드폰 가입 여부
    int countByLoginId(String loginId);
    // 닉네임 사용 수
    int countByAlias(String alias);
    // 프로필 수정 (닉네임 · 소개)
    void updateProfile(@Param("id") Long id, @Param("alias") String alias, @Param("bio") String bio);
    // 비밀번호 변경
    void updatePassword(@Param("id") Long id, @Param("memberPassword") String memberPassword);
}
