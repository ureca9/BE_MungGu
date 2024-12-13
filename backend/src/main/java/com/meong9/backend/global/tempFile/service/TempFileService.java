package com.meong9.backend.global.tempFile.service;

import com.meong9.backend.global.tempFile.entity.TempFile;
import com.meong9.backend.global.tempFile.repository.TempFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveTemporaryFile(String serviceUrl, MultipartFile file, Long id, String type) {
        try {
            TempFile temporaryFile = new TempFile(
                    serviceUrl,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes(),
                    id,
                    type
            );
            tempFileRepository.save(temporaryFile);
        } catch (IOException e) {
            log.error("임시 파일 저장 중 오류 발생. 서비스 URL: {}, 파일 이름: {}", serviceUrl, file.getOriginalFilename(), e);
            throw new RuntimeException("임시 파일 저장 실패", e);
        }
    }
}

