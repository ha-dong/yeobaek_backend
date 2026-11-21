package com.app.yeoback_backend.mapper;

import com.app.yeoback_backend.dto.TogetherMemberDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TogetherMemberMapper {
    // 모임에 참여시키기(모임장은 HOST, 참여자는 MEMBER)
    public void insert(TogetherMemberDTO togetherMemberDTO);
    // 참여 기록 한 개 보기
    public Optional<TogetherMemberDTO> selectById(Long id);
    // 이 회원이 이 모임에 이미 들어와 있는지 찾기
    public Optional<TogetherMemberDTO> selectByTogetherIdAndMemberId(@Param("togetherId") Long togetherId, @Param("memberId") Long memberId);
    // 모임의 참여자 목록
    public List<TogetherMemberDTO> selectAllByTogetherId(Long togetherId);
    // 지금 인원
    public int selectHeadcount(Long togetherId);
    // 모임에서 나가기
    public void delete(@Param("togetherId") Long togetherId, @Param("memberId") Long memberId);
}