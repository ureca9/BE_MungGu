package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.dto.RoomDto;
import com.meong9.backend.domain.pension.dto.RoomResponseDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.meong9.backend.jooq.generated.Tables.*;
import static com.meong9.backend.jooq.generated.Tables.ROOM_FILE;

@Repository
@RequiredArgsConstructor
public class RoomAvailabilityRepositoryImpl implements RoomAvailabilityRepository{

    private final DSLContext dsl; // jOOQ의 핵심 클래스, SQL 쿼리를 작성하고 실행하는 데 사용


    /**
     * 특정 펜션의 예약 가능한 방 목록을 조회하고, 각 방의 이미지 정보를 추가합니다.
     *
     * <p>이 메서드는 지정된 펜션 ID와 날짜 범위를 기준으로 예약 가능한 방 정보를 조회한 후,
     * 각 방에 연결된 이미지 URL 리스트를 조회하여 추가합니다.</p>
     *
     * @param pensionId 펜션 ID
     * @param startDate 예약 시작 날짜
     * @param endDate 예약 종료 날짜
     * @return 예약 가능한 방 목록 (각 방의 이미지 정보 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDto> findAvailableRoomsWithImages(Long pensionId, LocalDate startDate, LocalDate endDate) {
        List<RoomResponseDto> roomResponseDtos = new ArrayList<>();

        // 1. 예약 가능한 방 정보 조회
        // 지정된 펜션 ID와 날짜 범위 조건에 맞는 방 목록을 조회
        findAvailableRooms(pensionId, startDate, endDate).forEach(room -> {
            // 2. 각 방에 이미지 정보 추가
            // 조회된 방 목록에 대해 반복하면서 각 방의 방 ID를 사용해 이미지 URL 리스트를 조회하고 추가
            List<String> images = findRoomImages(room.getRoomId());

            // 조회된 이미지 URL 리스트를 RoomResponseDto에 설정
            roomResponseDtos.add(RoomResponseDto.builder().room(room).images(images).build());
        });

        // 3. 최종적으로 이미지가 추가된 방 목록 반환
        return roomResponseDtos;
    }

    /**
     * 특정 펜션의 예약 가능한 방 목록을 조회합니다.
     *
     * @param pensionId 펜션 ID
     * @param startDate 예약 시작 날짜
     * @param endDate 예약 종료 날짜
     * @return 예약 가능한 방의 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> findAvailableRooms(Long pensionId, LocalDate startDate, LocalDate endDate) {
        return dsl.select(
                        ROOM.ROOM_ID.as("roomId"),                     // 방 ID
                        ROOM.ROOM_NAME.as("roomName"),                // 방 이름
                        ROOM.AREA,                                    // 방 면적
                        ROOM.GUEST_COUNT.as("guestCount"),            // 기준 게스트 수
                        ROOM.PET_COUNT.as("petCount"),                // 기준 반려동물 수
                        ROOM.PRICE,                                   // 방 가격
                        ROOM.DESCRIPTION,                             // 방 설명
                        ROOM.START_TIME.as("startTime"),              // 입실 시간
                        ROOM.END_TIME.as("endTime"),                  // 퇴실 시간
                        ROOM.INFORMATION,                             // 방 정보
                        // 조건에 따른 매진 여부 계산
                        DSL.case_()
                                .when(DSL.count(ROOM_AVAILABILITY.DATE).eq(0), true) // 날짜 범위에 데이터가 없으면 true
                                .when(DSL.boolOr(ROOM_AVAILABILITY.IS_AVAILABLE.isFalse()), true) // 예약 가능이 하루라도 false면 true
                                .otherwise(false)                                   // 전부 true면 false
                                .as("isSoldOut")
                )
                .from(ROOM)
                .leftJoin(ROOM_AVAILABILITY).on(ROOM.ROOM_ID.eq(ROOM_AVAILABILITY.ROOM_ID))
                .where(ROOM.PENSION_ID.eq(pensionId))            // 지정된 펜션 ID 조건
                .and(ROOM_AVAILABILITY.DATE.between(startDate, endDate.minusDays(1))) // 날짜 범위 조건
                .groupBy(
                        ROOM.ROOM_ID,
                        ROOM.ROOM_NAME,
                        ROOM.AREA,
                        ROOM.GUEST_COUNT,
                        ROOM.PET_COUNT,
                        ROOM.PRICE,
                        ROOM.DESCRIPTION,
                        ROOM.START_TIME,
                        ROOM.END_TIME,
                        ROOM.INFORMATION
                )  // Room ID로 그룹화
                .fetchInto(RoomDto.class);              // 결과를 RoomDto로 매핑
    }


    /**
     * 특정 Room의 이미지 URL 리스트를 조회합니다.
     *
     * @param roomId Room ID
     * @return 해당 Room의 이미지 URL 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findRoomImages(Long roomId) {
        // ROOM_FILE과 MEDIA_FILE을 조인하여 fileUrl 조회
        return dsl.select(MEDIA_FILE.FILE_URL)                         // MediaFile 테이블의 fileUrl 선택
                .from(ROOM_FILE)                                    // RoomFile 테이블 기준
                .join(MEDIA_FILE).on(ROOM_FILE.MEDIA_FILE_ID.eq(MEDIA_FILE.MEDIA_FILE_ID)) // RoomFile과 MediaFile 조인
                .where(ROOM_FILE.ROOM_ID.eq(roomId))                // Room ID 조건
                .fetchInto(String.class);                          // 결과를 String 리스트로 반환
    }
}
