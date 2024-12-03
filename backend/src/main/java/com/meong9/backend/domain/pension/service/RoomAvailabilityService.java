package com.meong9.backend.domain.pension.service;

import com.meong9.backend.domain.pension.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {
    private final JdbcTemplate jdbcTemplate;

    private final RoomRepository roomRepository;

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

}