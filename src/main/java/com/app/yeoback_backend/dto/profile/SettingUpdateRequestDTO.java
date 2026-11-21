package com.app.yeoback_backend.dto.profile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * PATCH /api/profile/settings 요청 — 세부 설정 화면
 * 보낸 항목만 바꾼다 (null = 그대로). 취향·키워드는 보낼 때 목록 전체로 교체한다.
 */
@Getter @Setter @ToString
@NoArgsConstructor
public class SettingUpdateRequestDTO {
    private List<String> preferences;   // 여행 취향 이름 목록
    private List<String> vibes;         // 여행 키워드 이름 목록
    private Boolean aliasPublic;        // 닉네임 공개
    private Boolean phonePublic;        // 연락처 공개
    private Boolean noteAutoSave;       // 수첩 대화 자동 저장
    private String themeColor;          // 팔레트 id 또는 #rrggbb
}
