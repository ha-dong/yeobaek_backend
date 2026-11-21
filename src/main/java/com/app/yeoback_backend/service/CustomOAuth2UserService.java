package com.app.yeoback_backend.service;

import com.app.yeoback_backend.auth.OAuth2Attribute;
import com.app.yeoback_backend.common.enumeration.MemberRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * 공급자(카카오 · 네이버 · 구글)에서 받은 사용자 정보를 공통 형태(id, provider, email, name, profile)로 바꾼다.
 * 회원 조회 · 연동 · 가입은 로그인 성공 후 OAuth2SuccessHandler → OAuthLoginService 가 한다.
 * 여기서 붙이는 권한은 이 요청 안에서만 쓰이고, 이후 요청의 권한은 JWT 로 DB 에서 다시 읽는다.
 */
@Service
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        log.info("oauth user loaded: provider={}", provider);

        Map<String, Object> attributes = OAuth2Attribute.of(provider, userNameAttributeName, oAuth2User.getAttributes())
                .convertToMap();

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + MemberRole.MEMBER.name())),
                attributes, "id");
    }
}
