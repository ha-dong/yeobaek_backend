package com.app.yeoback_backend.controller.file;

import com.app.yeoback_backend.dto.FileDTO;
import com.app.yeoback_backend.mapper.FileMapper;
import com.app.yeoback_backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * 저장된 이미지 보여 주기: GET /api/files/{id}
 * 프로필 사진은 다른 회원도 보는 이미지라 로그인 없이 열린다 (SecurityConfig permitAll).
 * 파일 이름은 UUID 이고 사진을 바꾸면 id 도 바뀌므로 브라우저가 오래 캐시해도 된다.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileMapper fileMapper;
    private final FileStorageService fileStorageService;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable Long id) {
        FileDTO file = fileMapper.selectById(id).orElse(null);
        if (file == null) return ResponseEntity.notFound().build();

        Path path = fileStorageService.pathOf(file);
        if (!Files.isReadable(path)) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileStorageService.contentTypeOf(file)))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(new FileSystemResource(path));
    }
}
