package com.meong9.backend.global.utils;

import com.meong9.backend.domain.address.entity.PlcPenAddress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressMapper {

    public static String formatAddress(PlcPenAddress plcPenAddress) {
        if (plcPenAddress == null || plcPenAddress.getAddress() == null) {
            return null;
        }

        String province = plcPenAddress.getAddress().getProvince();
        String cityDistrict = plcPenAddress.getAddress().getCityDistrict();
        String subDistrict = plcPenAddress.getAddress().getSubDistrict();

        // Province가 4글자일 때 1번째, 3번째 추출 -> 충청남도 -> 충남
        if (province.length() == 4) {
            province = "" + province.charAt(0) + province.charAt(2);
        } else {
            // 그 외의 경우 앞 두 글자만 추출
            province = province.substring(0, Math.min(2, province.length()));
        }

        if(cityDistrict == null || cityDistrict.isBlank()) {
            if(subDistrict == null || subDistrict.isBlank()) {
                return province;
            }
            subDistrict = subDistrict.substring(0, Math.min(2, subDistrict.length()));
            return province + " " + subDistrict;
        } else {
            cityDistrict = cityDistrict.substring(0, Math.min(2, cityDistrict.length()));
            return province + " " + cityDistrict;
        }
    }

    public static String formatAddress(String province, String cityDistrict, String subDistrict) {

        // Province가 4글자일 때 1번째, 3번째 추출 -> 충청남도 -> 충남
        if (province.length() == 4) {
            province = "" + province.charAt(0) + province.charAt(2);
        } else {
            // 그 외의 경우 앞 두 글자만 추출
            province = province.substring(0, Math.min(2, province.length()));
        }

        if(cityDistrict == null || cityDistrict.isBlank()) {
            if(subDistrict == null || subDistrict.isBlank()) {
                return province;
            }
            subDistrict = subDistrict.substring(0, Math.min(2, subDistrict.length()));
            return province + " " + subDistrict;
        } else {
            cityDistrict = cityDistrict.substring(0, Math.min(2, cityDistrict.length()));
            return province + " " + cityDistrict;
        }
    }

    // 주소 정보 담는 Map
    public static Map<Long, String> mapAddressesByPlcPenId(List<PlcPenAddress> addresses) {
        Map<Long, String> addressMap = new HashMap<>();
        for (PlcPenAddress address : addresses) {
            Long id = address.getPlcPenId(); // 장소 ID
            String fullAddress = address.getAddress() != null ? address.getAddress().getAddress() : null;

            // PlcPenId와 Address를 맵에 추가
            addressMap.put(id, fullAddress);
        }
        return addressMap;
    }

    public static Map<Long, String> mapAddressesProvinceByPlcPenId(List<PlcPenAddress> addresses) {
        Map<Long, String> addressMap = new HashMap<>();

        // 주소 매핑 로직
        for (PlcPenAddress address : addresses) {
            Long id = address.getPlcPenId(); // 장소 ID
            String province = address.getAddress() != null ? address.getAddress().getProvince() : null;

            if (province != null) {
                // REGION_MAPPING에서 키를 찾음
                String regionKey = RegionMapper.getRegionMapping().entrySet().stream()
                        .filter(entry -> entry.getValue().stream().anyMatch(province::startsWith)) // 단어로 시작하는 경우 찾기
                        .map(Map.Entry::getKey) // 해당 키 가져오기
                        .findFirst() // 첫 번째 결과만 사용
                        .orElse(province); // 매칭되지 않을 경우 province
                addressMap.put(id, regionKey); // 매핑된 키로 저장
            } else {
                addressMap.put(id, "기타"); // Province가 null인 경우
            }
        }

        return addressMap;
    }

}
