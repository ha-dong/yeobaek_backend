package com.app.yeoback_backend.dto;

import com.app.yeoback_backend.domain.TogetherMemberVO;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime; //


@Getter @Setter @ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class TogetherMemberDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 참여 기록 번호
    private Long togetherId; //어느 모임인지
    private Long memberId; // 누가 들어왔는지
    private String togetherRole; //(모임장) / (참여자)
    private Integer togetherGuestMember; //데려오는 동행 수
    private LocalDateTime createdDatetime; // 들어온 시간
    private String memberAlias;
    public TogetherMemberVO toTogetherMemberVO() {
        return TogetherMemberVO.builder()
                .id(id)
                .togetherId(togetherId)
                .memberId(memberId)
                .togetherRole(togetherRole)
                .togetherGuestMember(togetherGuestMember)
                .build();
    }
}