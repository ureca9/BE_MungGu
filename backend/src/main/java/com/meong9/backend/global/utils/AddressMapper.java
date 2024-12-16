package com.meong9.backend.global.utils;

import com.meong9.backend.domain.address.entity.PlcPenAddress;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AddressMapper {

    // 지역 - province 캐시
    // Caffeine 캐시 설정: 최대 크기 1000, 10분 만료
    private static final Cache<String, String> provinceToRegionCache = Caffeine.newBuilder()
            .maximumSize(1000) // 최대 캐시 크기
            .expireAfterAccess(10, TimeUnit.MINUTES) // 마지막 접근 후 10분 만료
            .build();

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
        Map<String, List<String>> regionMapping = RegionMapper.getRegionMapping();

        for (PlcPenAddress address : addresses) {
            Long id = address.getPlcPenId();
            String province = address.getAddress() != null ? address.getAddress().getProvince() : null;

            if (province != null) {
                // 캐시된 결과가 있는지 확인
                String regionKey = provinceToRegionCache.get(province, p ->
                        regionMapping.entrySet().stream()
                                .filter(entry -> entry.getValue().stream().anyMatch(p::startsWith))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(p)
                );
                addressMap.put(id, regionKey);
            } else {
                addressMap.put(id, "기타");
            }
        }
        return addressMap;
    }

}
