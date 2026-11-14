package com.app.yeoback_backend.domain;

import com.app.yeoback_backend.audit.Period;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(of = "id", callSuper = false)
@SuperBuilder
public class TogetherVO extends Period {
    private Long id;
    private String togetherTitle;//모임 제목
    private String togetherContent;//모임 상세 내용(피그마 모바일18번)
    private String togetherPurpose;//모임의 장소(식당, 관광지)
    private String togetherArea;//모이는 장소에서 가까운 곳, 멀리
    private String togetherMaxMember;//모임의 최대 인원;
    private String togetherStart;//모임의 시작 시간
    private String togetherEnd;//모임의 끝나는 시간
    private String togetherStatus;// 모집 중, 마감, 종료
}
