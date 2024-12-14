package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.dto.RoomDto;
import com.meong9.backend.domain.pension.dto.RoomResponseDto;

import java.time.LocalDate;
import java.util.List;


public interface RoomAvailabilityRepository{
    List<RoomResponseDto> findAvailableRoomsWithImages(Long pensionId, LocalDate startDate, LocalDate endDate);
    List<RoomDto> findAvailableRooms(Long pensionId, LocalDate startDate, LocalDate endDate);
    List<String> findRoomImages(Long roomId);
}
