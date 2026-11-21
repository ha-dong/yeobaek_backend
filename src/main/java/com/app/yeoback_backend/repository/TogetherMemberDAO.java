package com.app.yeoback_backend.repository;

import com.app.yeoback_backend.dto.TogetherMemberDTO;
import com.app.yeoback_backend.mapper.TogetherMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TogetherMemberDAO {
    private final TogetherMemberMapper togetherMemberMapper;
    // 모임에 참여시키기
    public void save(TogetherMemberDTO togetherMemberDTO) {
        togetherMemberMapper.insert(togetherMemberDTO);
    }
    // 참여 기록 한 개 보기
    public Optional<TogetherMemberDTO> findById(Long id) {
        return togetherMemberMapper.selectById(id);
    }
    // 이 회원이 이 모임에 이미 들어와 있는지 찾기
    public Optional<TogetherMemberDTO> findByTogetherIdAndMemberId(Long togetherId, Long memberId) {
        return togetherMemberMapper.selectByTogetherIdAndMemberId(togetherId, memberId);
    }
    // 모임의 참여자 목록
    public List<TogetherMemberDTO> findAllByTogetherId(Long togetherId) {
        return togetherMemberMapper.selectAllByTogetherId(togetherId);
    }
    // 지금 인원
    public int findHeadcount(Long togetherId) {
        return togetherMemberMapper.selectHeadcount(togetherId);
    }
    // 모임에서 나가기
    public void delete(Long togetherId, Long memberId) {
        togetherMemberMapper.delete(togetherId, memberId);
    }
}