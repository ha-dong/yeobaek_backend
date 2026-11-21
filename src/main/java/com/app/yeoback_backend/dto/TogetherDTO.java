package com.app.yeoback_backend.dto;

import com.app.yeoback_backend.domain.TogetherVO;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter @Setter @ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class TogetherDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long placeId;
    private String togetherTitle;
    private String togetherContent;
    private String togetherPurpose; // (식당) /(관광지)
    private String togetherArea; // (가까운 곳) / (먼 곳)
    private Integer togetherMaxMember;
    private LocalDateTime togetherStartDate;
    private LocalDateTime togetherEndDate;
    private String togetherStatus; // (모집 중) / (마감) / (완료)
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;

    public TogetherVO toTogetherVO() {
        return TogetherVO.builder()
                .id(id)
                .placeId(placeId)
                .togetherTitle(togetherTitle)
                .togetherContent(togetherContent)
                .togetherPurpose(togetherPurpose)
                .togetherArea(togetherArea)
                .togetherMaxMember(togetherMaxMember)
                .togetherStartDate(togetherStartDate)
                .togetherEndDate(togetherEndDate)
                .togetherStatus(togetherStatus)
                .build();
    }
}