package com.meong9.backend.domain.recommendation.recommendation.projection;

public interface PlcPenProjection {
    Long getId();
    Double getLatitude();
    Double getLongitude();
    String getProvince();

    default String toCustomString() {
        return "PlcPenProjection{" +
                "id=" + getId() +
                ", latitude=" + getLatitude() +
                ", longitude=" + getLongitude() +
                ", province=" + getProvince() +
                '}';
    }
}
