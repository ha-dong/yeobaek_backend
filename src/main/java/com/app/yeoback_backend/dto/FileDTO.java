package com.app.yeoback_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * tbl_file 한 행. 실제 파일은 {file.upload-dir}/{filePath}/{fileName} 에 있다.
 *  - filePath : 날짜 폴더 (예: 2026/11/20)
 *  - fileName : 저장용 이름 (UUID + 확장자)
 */
@Getter @Setter @ToString
@NoArgsConstructor
public class FileDTO {
    private Long id;
    private String originalName;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String createdDatetime;
}
