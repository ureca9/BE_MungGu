package com.meong9.backend.domain.pension.service;

import com.meong9.backend.domain.pension.dto.PensionSummaryResponseDto;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PensionService {
    private final PensionRepository pensionRepository;

    public PensionSummaryResponseDto getPensionSummaryResponseDto(Long pensionId) {
        return pensionRepository.findPensionSummaryResponseDtoById(pensionId)
                .orElseThrow(() -> NotFoundException.entityNotFound("펜션"));
    }
}
