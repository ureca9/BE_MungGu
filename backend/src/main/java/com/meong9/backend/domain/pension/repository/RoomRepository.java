package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room,Long> {
    @Query("SELECT r.roomId FROM Room r")
    List<Long> findAllIds(); // JPQL로 방 ID만 조회
}
