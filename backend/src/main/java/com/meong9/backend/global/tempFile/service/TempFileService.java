package com.meong9.backend.global.tempFile.service;

import com.meong9.backend.global.tempFile.entity.TempFile;
import com.meong9.backend.global.tempFile.repository.TempFileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TempFileService")
public class TempFileService {

    private final TempFileRepository tempFileRepository;

    /**
     * 바이너리 파일을 임시 테이블에 저장
     */
    @Transactional
    public void saveTemporaryFile(String serviceUrl, MultipartFile file) {
        try {
            TempFile temporaryFile = new TempFile(
                    serviceUrl,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
            tempFileRepository.save(temporaryFile);
        } catch (IOException e) {
            log.error("임시 파일 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("임시 파일 저장 실패", e);
        }
    }
}

