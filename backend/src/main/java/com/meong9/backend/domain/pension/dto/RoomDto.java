package com.meong9.backend.domain.pension.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RoomDto {
    private final Long roomId;       // Room ID
    private final String roomName;   // 방 이름
    private final Integer area;      // 방 면적
    private final Integer guestCount; // 기준 게스트 수
    private final Integer petCount;  // 기준 반려동물 수
    private final Integer price;     // 방 가격
    private final String description; // 방 설명
    private final String startTime;  // 입실 시간
    private final String endTime;    // 퇴실 시간
    private final String information; // 방 정보
    private final Boolean isSoldOut;  // 매진 여부
}
