package com.app.yeoback_backend.service;

import com.app.yeoback_backend.aop.annotation.LogStatus;
import com.app.yeoback_backend.dto.ChatDTO;
import com.app.yeoback_backend.dto.TogetherDTO;
import com.app.yeoback_backend.dto.TogetherMemberDTO;
import com.app.yeoback_backend.repository.ChatDAO;
import com.app.yeoback_backend.repository.TogetherDAO;
import com.app.yeoback_backend.repository.TogetherMemberDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // 메서드 안의 DB 작업을 한 묶음으로 (중간에 오류 나면 전부 되돌림)
public class TogetherService {
    private final TogetherDAO togetherDAO;
    private final TogetherMemberDAO togetherMemberDAO;
    private final ChatDAO chatDAO;

    // 모임 만들기 (memberId = 로그인한 사람)
    @LogStatus
    public Long create(TogetherDTO togetherDTO, Long memberId) {
        check(togetherDTO); // [추가] 규칙 2·3·4 검사
        togetherDAO.save(togetherDTO); // 시작 시간은 XML 에서 now() 로 들어감 (규칙 6)

        TogetherMemberDTO host = new TogetherMemberDTO(); // 만든 사람을 모임장으로 등록 (규칙 1)
        host.setTogetherId(togetherDTO.getId()); // [수정] togetherDAO.getId() → togetherDTO.getId() (새 번호는 DTO 에 들어옴)
        host.setMemberId(memberId);
        host.setTogetherRole("HOST");
        togetherMemberDAO.save(host);

        ChatDTO chatDTO = new ChatDTO(); // 모임 채팅방 만들기
        chatDTO.setTogetherId(togetherDTO.getId());
        chatDAO.save(chatDTO);

        return togetherDTO.getId(); // [수정] 빠져 있던 return 추가
    }

    // [추가] 모임 한 개 보기
    public TogetherDTO getTogether(Long id) {
        return togetherDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없어요."));
    }

    // [추가] 모임 목록
    public List<TogetherDTO> getList() {
        return togetherDAO.findAll();
    }

    // [추가] 모임 고치기 (규칙 1: 모임장만)
    @LogStatus
    public void modify(TogetherDTO togetherDTO, Long memberId) {
        checkHost(togetherDTO.getId(), memberId);
        check(togetherDTO);
        if (togetherDTO.getTogetherMaxMember() < togetherDAO.findMemberCount(togetherDTO.getId())) { // 지금 인원보다 적게 줄이면
            throw new IllegalArgumentException("지금 들어온 사람보다 적게 줄일 수 없어요.");
        }
        togetherDAO.update(togetherDTO);
    }

    // [추가] 규칙 5: 모집 중(OPEN) / 마감(CLOSE) / 완료(ENDED) 바꾸기 (모임장만)
    @LogStatus
    public void changeStatus(Long togetherId, Long memberId, String togetherStatus) {
        checkHost(togetherId, memberId);
        if (!"OPEN".equals(togetherStatus) && !"CLOSE".equals(togetherStatus) && !"ENDED".equals(togetherStatus)) {
            throw new IllegalArgumentException("모집 중, 마감, 완료 중에서 골라 주세요.");
        }
        togetherDAO.updateStatus(togetherId, togetherStatus);
    }

    // 규칙 8: 2시간 동안 대화가 없는 모임 끝내기 (TogetherScheduler 가 5분마다 부름)
    public void endFinished() {
        togetherDAO.updateStatusToEnded();
    }

    // 모임 들어가기
    @LogStatus
    public void join(Long togetherId, Long memberId) {
        TogetherDTO together = getTogether(togetherId); // 없는 모임이면 여기서 멈춤
        if (!"OPEN".equals(together.getTogetherStatus())) { // 규칙 5: 모집 중일 때만
            throw new IllegalArgumentException("모집 중인 모임만 들어갈 수 있어요.");
        }
        if (togetherMemberDAO.findByTogetherIdAndMemberId(togetherId, memberId).isPresent()) { // 같은 모임은 한 번만
            throw new IllegalArgumentException("이미 들어온 모임이에요.");
        }
        if (togetherDAO.findMemberCount(togetherId) >= together.getTogetherMaxMember()) { // 규칙 4: 꽉 찼으면
            throw new IllegalArgumentException("모임 인원이 다 찼어요.");
        }
        TogetherMemberDTO member = new TogetherMemberDTO();
        member.setTogetherId(togetherId);
        member.setMemberId(memberId);
        member.setTogetherRole("MEMBER"); // 화면에는 프로필 닉네임이 보임 (규칙 7)
        togetherMemberDAO.save(member);
    }

    // 모임 나가기
    @LogStatus
    public void leave(Long togetherId, Long memberId) {
        if (memberId.equals(togetherDAO.findHostId(togetherId))) { // 모임장이면
            throw new IllegalArgumentException("모임장은 나갈 수 없어요. 모임을 '완료'로 바꿔 주세요.");
        }
        togetherMemberDAO.delete(togetherId, memberId);
    }

    // [추가] 규칙 1: 모임장인지 확인
    private void checkHost(Long togetherId, Long memberId) {
        Long hostId = togetherDAO.findHostId(togetherId);
        if (hostId == null || !hostId.equals(memberId)) {
            throw new IllegalArgumentException("모임장만 할 수 있어요.");
        }
    }

    // [추가] 규칙 2·3·4: 만들기·고치기 때 입력 검사 ("MEAL".equals(값) 처럼 글자를 앞에 쓰면 값이 null 이어도 오류가 안 남)
    private void check(TogetherDTO togetherDTO) {
        String purpose = togetherDTO.getTogetherPurpose();
        if (!"MEAL".equals(purpose) && !"SPOT".equals(purpose)) { // 규칙 2
            throw new IllegalArgumentException("식당 또는 관광지 중 하나를 골라 주세요.");
        }
        String area = togetherDTO.getTogetherArea();
        if (!"NEAR".equals(area) && !"OTHER".equals(area)) { // 규칙 3
            throw new IllegalArgumentException("가까운 곳 또는 먼 곳 중 하나를 골라 주세요.");
        }
        Integer max = togetherDTO.getTogetherMaxMember();
        if (max == null || max < 2 || max > 6) { // 규칙 4 (DB 도 2~6명만 받음)
            throw new IllegalArgumentException("최대 인원은 2명부터 6명까지예요.");
        }
    }
}