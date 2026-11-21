package com.app.yeoback_backend.service;

import com.app.yeoback_backend.common.exception.ApiException;
import com.app.yeoback_backend.dto.FileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * 이미지 파일 로컬 저장 (S3 대신)
 *  - 저장 위치: {file.upload-dir}/yyyy/MM/dd/{UUID}.{확장자}
 *  - 이미지(jpeg · png · webp · gif)만, 5MB 이하
 *  - DB(tbl_file) 기록은 호출하는 쪽(ProfileService)이 한다
 */
@Service
@Slf4j
public class FileStorageService {
    private static final long MAX_SIZE = 5L * 1024 * 1024;
    private static final Map<String, String> IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final Path root;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /** 이미지를 디스크에 저장하고 tbl_file 에 넣을 정보를 돌려준다 (id 는 아직 없음) */
    public FileDTO saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "사진을 선택해 주세요.");
        }
        String ext = file.getContentType() == null ? null : IMAGE_TYPES.get(file.getContentType().toLowerCase());
        if (ext == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "jpg · png · webp · gif 사진만 올릴 수 있어요.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "too_large", "사진은 5MB 이하만 올릴 수 있어요.");
        }

        String datePath = LocalDate.now().format(DATE_DIR);
        String fileName = UUID.randomUUID() + "." + ext;
        Path target = resolve(datePath, fileName);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", target, e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "save_failed", "사진을 저장하지 못했어요.");
        }

        FileDTO dto = new FileDTO();
        dto.setOriginalName(cleanName(file.getOriginalFilename(), fileName));
        dto.setFileName(fileName);
        dto.setFilePath(datePath);
        dto.setFileSize(file.getSize());
        return dto;
    }

    /** 저장된 파일의 실제 경로 (업로드 폴더 밖을 가리키면 거절) */
    public Path pathOf(FileDTO file) {
        return resolve(file.getFilePath(), file.getFileName());
    }

    /** 디스크에서 삭제 (없으면 무시) */
    public void delete(FileDTO file) {
        try {
            Files.deleteIfExists(pathOf(file));
        } catch (IOException | RuntimeException e) {
            log.warn("파일 삭제 실패: id={} ({})", file.getId(), e.getMessage());
        }
    }

    /** 확장자로 Content-Type 결정 */
    public String contentTypeOf(FileDTO file) {
        String name = file.getFileName().toLowerCase();
        return IMAGE_TYPES.entrySet().stream()
                .filter(e -> name.endsWith("." + e.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse("application/octet-stream");
    }

    private Path resolve(String datePath, String fileName) {
        Path path = root.resolve(datePath).resolve(fileName).normalize();
        if (!path.startsWith(root)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "잘못된 파일 경로예요.");
        }
        return path;
    }

    /** 원래 파일 이름은 기록용으로만 쓴다: 경로 문자 제거, 255자 제한 */
    private String cleanName(String original, String fallback) {
        if (original == null || original.isBlank()) return fallback;
        String name = original.replaceAll("[\\\\/:*?\"<>|]", "_");
        return name.length() > 255 ? name.substring(name.length() - 255) : name;
    }
}
