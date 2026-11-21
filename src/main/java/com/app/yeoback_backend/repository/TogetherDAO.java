package com.app.yeoback_backend.repository; // [추가 파일]

import com.app.yeoback_backend.dto.TogetherDTO;
import com.app.yeoback_backend.mapper.TogetherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TogetherDAO {
    private final TogetherMapper togetherMapper;

    // 모임 만들기 (끝나면 togetherDTO 의 id 에 새 번호가 들어옴)
    public void save(TogetherDTO togetherDTO) {
        togetherMapper.insert(togetherDTO);
    }
    // 모임 한 개 보기
    public Optional<TogetherDTO> findById(Long id) {
        return togetherMapper.selectById(id);
    }
    // 모임 목록
    public List<TogetherDTO> findAll() {
        return togetherMapper.selectAll();
    }
    // 모임장 번호
    public Long findHostId(Long id) {
        return togetherMapper.selectHostId(id);
    }
    // 지금 인원
    public int findMemberCount(Long id) {
        return togetherMapper.selectMemberCount(id);
    }
    // 모임 고치기
    public void update(TogetherDTO togetherDTO) {
        togetherMapper.update(togetherDTO);
    }
    // 상태 바꾸기
    public void updateStatus(Long id, String togetherStatus) {
        togetherMapper.updateStatus(id, togetherStatus);
    }
    // 조용한 모임 끝내기
    public void updateStatusToEnded() {
        togetherMapper.updateStatusToEnded();
    }
}