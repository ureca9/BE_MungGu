package com.meong9.backend.domain.pension.service;

import com.meong9.backend.domain.pension.dto.RoomResponseDto;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.pension.repository.RoomAvailabilityRepository;
import com.meong9.backend.domain.pension.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {
    private final JdbcTemplate jdbcTemplate;

    private final RoomRepository roomRepository;

    private final RoomAvailabilityRepository roomAvailabilityRepository;

    /**
     * Room의 RoomAvailability를 days만큼 추가합니다.
     *
     * @param days
     */
    @Transactional
    public void addAvailabilityForDays(int days) {
        List<Long> roomIds = roomRepository.findAllIds(); // 실제 존재하는 방 ID 가져오기
        LocalDate today = LocalDate.now();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        String sql = "INSERT INTO room_availability (created_at, modified_at, date, is_available, room_id) " +
                "SELECT ?, ?, ?, ?, ? WHERE NOT EXISTS (" +
                "SELECT 1 FROM room_availability WHERE date = ? AND room_id = ?)";

        for (Long roomId : roomIds) {
            for (int i = 1; i <= days; i++) {
                LocalDate date = today.plusDays(i);
                jdbcTemplate.update(sql, now, now, date, true, roomId, date, roomId);
            }
        }
    }

    public List<RoomResponseDto> getRoomAvailableRoomsWithImages(Long pensionId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(LocalDate.now()) || endDate.isBefore(startDate) || ChronoUnit.DAYS.between(startDate, endDate) > 30) {
            // 시작일은 현재 날짜보다 이전일 수 없다, 종료일은 시작일보다 이전일 수 없다, 예약 기간은 30일을 초과할 수 없다.
            return new ArrayList<>();
        }else{
            return roomAvailabilityRepository.findAvailableRoomsWithImages(pensionId, startDate, endDate);
        }
    }
}