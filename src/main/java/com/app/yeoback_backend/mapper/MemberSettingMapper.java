package com.app.yeoback_backend.mapper;

import com.app.yeoback_backend.dto.MemberSettingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberSettingMapper {
    // 기본값으로 설정 행 만들기 (이미 있으면 그대로)
    void insertDefault(Long memberId);
    // 설정 조회
    Optional<MemberSettingDTO> selectByMemberId(Long memberId);
    // 설정 수정 (null 인 항목은 그대로)
    void update(MemberSettingDTO setting);

    // 여행 취향
    List<String> selectPreferenceNames(Long memberId);
    List<Long> selectPreferenceIdsByNames(@Param("names") List<String> names);
    void deletePreferences(Long memberId);
    void insertPreferences(@Param("memberId") Long memberId, @Param("ids") List<Long> ids);

    // 여행 키워드
    List<String> selectVibeNames(Long memberId);
    List<Long> selectVibeIdsByNames(@Param("names") List<String> names);
    void deleteVibes(Long memberId);
    void insertVibes(@Param("memberId") Long memberId, @Param("ids") List<Long> ids);
}
