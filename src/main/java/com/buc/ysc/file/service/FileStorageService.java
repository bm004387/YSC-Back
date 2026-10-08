package com.buc.ysc.file.service;

import com.buc.ysc.file.mapper.FileMapper;
import com.buc.ysc.file.vo.StoredFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

/** 공통 파일 저장소: 파일 시스템에 저장하고 FIL_BAS/FIL_DTL에 메타데이터를 기록합니다. */
@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final FileMapper fileMapper;
    private final Path storageRoot;

    public FileStorageService(FileMapper fileMapper,
                              @Value("${file.storage.root:./uploads}") String storageRoot) {
        this.fileMapper = fileMapper;
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    @Transactional
    public Long store(MultipartFile file, String fileType, String fileCode, String userId) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "저장할 파일을 선택해 주세요.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "파일은 10MB 이하로 선택해 주세요.");
        }

        Long fileSeq = fileMapper.nextFileSeq();
        String originalName = safeOriginalName(file.getOriginalFilename());
        String extension = extensionOf(originalName);
        String savedName = UUID.randomUUID() + extension;
        Path directory = storageRoot.resolve(fileSeq.toString()).normalize();
        Path savedPath = directory.resolve(savedName).normalize();
        ensureInsideRoot(savedPath);

        try {
            Files.createDirectories(directory);
            file.transferTo(savedPath);
            fileMapper.insertFileBase(fileSeq, fileType, fileCode, directory.toString(), userId);
            fileMapper.insertFileDetail(fileSeq, 1L, originalName, savedName, extension,
                    file.getSize(), safeContentType(file.getContentType()), userId);
            return fileSeq;
        } catch (IOException | RuntimeException exception) {
            try { Files.deleteIfExists(savedPath); } catch (IOException ignored) { }
            if (exception instanceof ResponseStatusException statusException) throw statusException;
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다.", exception);
        }
    }

    public StoredFile metadata(Long fileSeq) {
        StoredFile file = fileMapper.selectFile(fileSeq);
        if (file == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.");
        return file;
    }

    public Path pathOf(StoredFile file) {
        Path directory = Path.of(file.getFilPth()).toAbsolutePath().normalize();
        Path path = directory.resolve(file.getSavFilNm()).normalize();
        ensureInsideRoot(path);
        if (!Files.isRegularFile(path)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.");
        return path;
    }

    @Transactional
    public void delete(Long fileSeq) {
        if (fileSeq == null) return;
        StoredFile file = fileMapper.selectFile(fileSeq);
        if (file == null) return;
        Path path = pathOf(file);
        try {
            Files.deleteIfExists(path);
            Path directory = path.getParent();
            if (directory != null) Files.deleteIfExists(directory);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "기존 파일 삭제에 실패했습니다.", exception);
        }
        fileMapper.deleteFileDetails(fileSeq);
        fileMapper.deleteFileBase(fileSeq);
    }

    private void ensureInsideRoot(Path path) {
        if (!path.normalize().startsWith(storageRoot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 파일 경로입니다.");
        }
    }

    private String safeOriginalName(String name) {
        String value = name == null ? "upload" : Path.of(name).getFileName().toString();
        return value.length() > 255 ? value.substring(value.length() - 255) : value;
    }

    private String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "";
        String extension = name.substring(dot).replaceAll("[^A-Za-z0-9.]", "").toLowerCase(Locale.ROOT);
        return extension.length() > 20 ? extension.substring(0, 20) : extension;
    }

    private String safeContentType(String contentType) {
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }
}
