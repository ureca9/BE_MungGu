package com.meong9.backend.domain.pension.dto;

import lombok.*;

import java.util.List;


/**
 * Jooq의 DTO 값 매핑
 * 1. 기본 생성자를 호출한 후, setter 메서드나 리플렉션을 통해 값을 설정.
 * 2. 필드가 final일 경우, 모든 필드를 초기화하는 생성자를 통해 객체 생성.
 */
@Getter
@Builder
@AllArgsConstructor
public class RoomResponseDto {
    private final RoomDto room;
    private final List<String> images; // 방에 대한 이미지 URL 리스트
}