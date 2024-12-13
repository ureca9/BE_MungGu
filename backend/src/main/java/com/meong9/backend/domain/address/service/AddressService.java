package com.meong9.backend.domain.address.service;

import com.meong9.backend.domain.address.entity.Address;
import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 특정 pensionId 리스트에 대해 Address 데이터를 조회하고 Map 형태로 반환합니다.
     *
     * @param pensionIds Address를 조회할 Pension ID 리스트
     * @param type Address 조회에 필요한 유형 정보 (예: "020")
     * @return pensionId를 Key로, Address 객체를 Value로 하는 Map
     */
    @Transactional(readOnly = true)
    public Map<Long, Address> getAddressesForPensionsOrPlaces(List<Long> pensionIds, String type) {
        // PlcPenAddressRepository를 통해 PlcPenAddress 리스트 조회
        List<PlcPenAddress> plcPenAddresses = plcPenAddressRepository.findByPlcPenIdsAndType(pensionIds, type);

        // PlcPenAddress 리스트를 Stream으로 변환하여 pensionId를 Key, Address를 Value로 Map 생성
        return plcPenAddresses.stream()
                .collect(Collectors.toMap(
                        PlcPenAddress::getPlcPenId,   // Key: pensionId
                        PlcPenAddress::getAddress    // Value: Address 객체
                ));
    }

}
