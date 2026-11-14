package com.app.yeoback_backend.mapper;

import com.app.yeoback_backend.domain.MemberVO;
import com.app.yeoback_backend.dto.MemberDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface TogetherMapper {

    public void insert(TogetherDTO memberDTO);

    public Optional<MemberDTO> selectMemberForLogin(MemberVO memberVO);

}
