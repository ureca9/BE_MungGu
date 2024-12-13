package com.meong9.backend.global.tempFile.controller;

import com.meong9.backend.global.dto.CommonResponse;
import com.meong9.backend.global.tempFile.service.TempFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class TempFileController {

    private final TempFileService tempFileService;

    @PostMapping("/restore/meong-photo")
    public ResponseEntity<?> restoreTemporaryFiles() {
        tempFileService.saveMeongPhotoToS3FromTempFile();
        return CommonResponse.ok("임시 파일 복구 작업 완료");
    }
}
