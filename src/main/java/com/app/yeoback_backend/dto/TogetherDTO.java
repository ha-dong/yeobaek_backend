package com.app.yeoback_backend.dto;

import com.app.yeoback_backend.common.enumeration.Status;
import com.app.yeoback_backend.domain.MemberVO;
import com.app.yeoback_backend.domain.TogetherVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter @Setter @ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class TogetherDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String togetherTitle;//모임 제목
    private String togetherContent;//모임 상세 내용(피그마 모바일18번)
    private String togetherPurpose;//모임의 장소(식당, 관광지)
    private String togetherArea;//모이는 장소에서 가까운 곳, 멀리
    private String togetherMaxMember;//모임의 최대 인원;
    private String togetherStart;//모임의 시작 시간
    private String togetherEnd;//모임의 끝나는 시간
    private String togetherStatus;// 모집 중, 마감, 종료

    public TogetherVO toTogetherVO(){
        return TogetherVO.builder()
                .id(id)
                .togetherTitle(togetherTitle)
                .togetherContent(togetherContent)
                .togetherPurpose(togetherPurpose)
                .togetherArea(togetherArea)
                .togetherMaxMember(togetherMaxMember)
                .togetherStart(togetherStart)
                .togetherStart(togetherStart)
                .togetherEnd(togetherEnd)
                .togetherStatus(togetherStatus)
                .build();
    }

}


