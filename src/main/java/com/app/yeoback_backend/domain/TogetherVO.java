package com.app.yeoback_backend.domain;

import com.app.yeoback_backend.audit.Period;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(of = "id", callSuper = false)
@SuperBuilder
public class TogetherVO extends Period {
    private Long id;
    private Long placeId;
    private String togetherTitle;
    private String togetherContent;
    private String togetherPurpose;
    private String togetherArea;
    private Integer togetherMaxMember;
    private LocalDateTime togetherStartDate;
    private LocalDateTime togetherEndDate;
    private String togetherStatus;
}