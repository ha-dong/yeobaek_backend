package com.app.yeoback_backend.mapper;

import com.app.yeoback_backend.dto.FileDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface FileMapper {
    // 파일 정보 저장 (id 생성)
    void insert(FileDTO file);
    Optional<FileDTO> selectById(Long id);
    void deleteById(Long id);

    // 회원 프로필 이미지 (tbl_file_member)
    void insertMemberFile(@Param("fileId") Long fileId, @Param("memberId") Long memberId);
    Optional<FileDTO> selectMemberFile(Long memberId);
    void deleteMemberFile(Long fileId);
}
