package com.app.yeoback_backend.domain;

import com.app.yeoback_backend.audit.Period;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

// TogetherVO 와 같은 모양 (만든 시간은 부모 Period 에 있음)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(of = "id", callSuper = false)
@SuperBuilder
public class TogetherMemberVO extends Period {
    private Long id;
    private Long togetherId;
    private Long memberId;
    private String togetherRole; // (모임장) / (참여자)
    private Integer togetherGuestMember; // 데려오는 동행 수
}