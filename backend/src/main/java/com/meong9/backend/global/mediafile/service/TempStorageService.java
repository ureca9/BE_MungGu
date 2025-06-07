package com.meong9.backend.global.mediafile.service;

import com.meong9.backend.global.mediafile.entity.EncodingFailure;
import com.meong9.backend.global.mediafile.repository.EncodingFailureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TempStorageService {
    private final EncodingFailureRepository failureRepo;

    // 인코딩/업로드 재시도 후에도 실패한 요청 정보를 기록
    public void recordFailure(Long reviewId, String filename, LocalDateTime occurredAt, String reason) {
        EncodingFailure failure = EncodingFailure.builder()
                .reviewId(reviewId)
                .filename(filename)
                .occurredAt(occurredAt)
                .reason(reason)
                .build();
        failureRepo.save(failure);
    }
}
