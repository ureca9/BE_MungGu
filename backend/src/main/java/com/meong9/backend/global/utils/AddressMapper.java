package com.meong9.backend.global.utils;

import com.meong9.backend.domain.address.entity.PlcPenAddress;

public class AddressMapper {

    public static String formatAddress(PlcPenAddress plcPenAddress) {
        if (plcPenAddress == null || plcPenAddress.getAddress() == null) {
            return null;
        }

        String province = plcPenAddress.getAddress().getProvince();
        String cityDistrict = plcPenAddress.getAddress().getCityDistrict();

        // Province가 4글자일 때 1번째, 3번째 추출 -> 충청남도 -> 충남
        if (province.length() == 4) {
            province = "" + province.charAt(0) + province.charAt(2);
        } else {
            // 그 외의 경우 앞 두 글자만 추출
            province = province.substring(0, Math.min(2, province.length()));
        }
        cityDistrict = cityDistrict.substring(0, Math.min(2, cityDistrict.length()));

        return province + " " + cityDistrict;
    }

}
