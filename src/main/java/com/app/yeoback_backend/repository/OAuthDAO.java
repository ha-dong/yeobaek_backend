package com.app.yeoback_backend.repository;

import com.app.yeoback_backend.common.enumeration.OAuthProvider;
import com.app.yeoback_backend.domain.OAuthVO;
import com.app.yeoback_backend.dto.MemberDTO;
import com.app.yeoback_backend.mapper.OAuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OAuthDAO {
    private final OAuthMapper oAuthMapper;

    //    회원가입
    public void save(OAuthVO oAuthVO){
        oAuthMapper.insert(oAuthVO);
    }
    //    공급자 + 공급자 고유 id 로 조회
    public Optional<MemberDTO> findMemberByProviderId(OAuthProvider provider, String providerId){
        return oAuthMapper.selectMemberByProviderId(provider, providerId);
    }
    //    이메일로 조회
    public Optional<MemberDTO> findMemberByMemberLoginId(String loginId, OAuthProvider provider){
        return oAuthMapper.selectMemberByLoginId(loginId, provider);
    }
}

