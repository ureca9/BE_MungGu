package com.meong9.backend.domain.pension.service;

import com.meong9.backend.domain.pension.dto.PensionInfoDto;
import com.meong9.backend.domain.pension.dto.PensionSummaryResponseDto;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PensionService {
    private final PensionRepository pensionRepository;

    @Transactional(readOnly = true)
    public PensionSummaryResponseDto getPensionSummaryResponseDto(Long pensionId) {
        return pensionRepository.findPensionSummaryResponseDtoById(pensionId)
                .orElseThrow(() -> NotFoundException.entityNotFound("펜션"));
    }

    /**
     * 주어진 펜션 ID에 해당하는 PensionInfoDto를 조회합니다.
     * 해당 ID로 Pension 엔티티를 찾을 수 없는 경우 NotFoundException을 발생시킵니다.
     *
     * @param pensionId 조회할 Pension 엔티티의 고유 식별자
     * @return 제공된 펜션 ID에 해당하는 PensionInfoDto
     * @throws NotFoundException 제공된 ID로 Pension 엔티티를 찾을 수 없는 경우 발생
     */
    @Transactional(readOnly = true)
    public PensionInfoDto getPensionInfo(Long pensionId, Long memberId){
        return pensionRepository.findPensionInfoByIdWithLikeStatus(pensionId, memberId).
                orElseThrow(() -> NotFoundException.entityNotFound("펜션"));
    }
}
