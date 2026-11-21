package com.app.yeoback_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * tbl_member_setting 한 행 (id = 회원 id)
 * 수정할 때는 null 인 항목은 바꾸지 않는다 (MemberSettingMapper.update)
 */
@Getter @Setter @ToString
@NoArgsConstructor
public class MemberSettingDTO {
    private Long id;
    private String themaColor;
    private Boolean aliasPublic;
    private Boolean phonePublic;
    private Boolean noteAutoSave;
}
