package com.app.yeoback_backend.service;

import com.app.yeoback_backend.dto.TogetherMemberDTO;
import com.app.yeoback_backend.repository.TogetherMemberDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TogetherMemberService {
    private final TogetherMemberDAO togetherMemberDAO;
    //모임 참여자 목록 닉네임으로만 보이게
    public List<TogetherMemberDTO> getMembers(Long togetherId) {
        return togetherMemberDAO.findAllByTogetherId(togetherId);
    }
}