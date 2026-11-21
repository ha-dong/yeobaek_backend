package com.app.yeoback_backend.mapper;

import com.app.yeoback_backend.dto.TogetherDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TogetherMapper {
    public void insert(TogetherDTO togetherDTO);
    public Optional<TogetherDTO> selectById(Long id);
    public List<TogetherDTO> selectAll();
    public Long selectHostId(Long id);
    public int selectMemberCount(Long id);
    public void update(TogetherDTO togetherDTO);
    public void updateStatus(@Param("id") Long id, @Param("togetherStatus") String togetherStatus);
    public void updateStatusToEnded();

}