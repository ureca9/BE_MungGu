package com.meong9.backend.domain.address.service;

import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final PlcPenAddressRepository plcPenAddressRepository;

    /**
     * 장소에 연결된 주소 정보를 조회하는 메서드
     * @param placeId 장소 ID
     * @param type 주소 타입
     * @return 주소 문자열 (없을 경우 "주소 정보 없음")
     */
    @Transactional(readOnly = true)
    public String getAddress(Long placeId, String type) {
        return plcPenAddressRepository.findFullAddress(type, placeId).
                orElseThrow(() -> NotFoundException.entityNotFound("type: " +type+", place_id: " + placeId));
    }
}
