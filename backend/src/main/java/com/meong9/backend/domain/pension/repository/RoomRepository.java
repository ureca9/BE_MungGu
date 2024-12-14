package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room,Long> {
    @Query("SELECT r.roomId FROM Room r")
    List<Long> findAllIds(); // JPQL로 방 ID만 조회
    /**
     * 특정 pensionId 리스트에 대해 평균 객실 가격을 조회합니다.
     *
     * @param pensionIds 평균 가격을 조회할 Pension ID 리스트
     * @return 각 pensionId와 해당 평균 가격(Object 배열 형태)
     */
    @Query("SELECT r.pension.pensionId, AVG(r.price) " +
            "FROM Room r " +
            "WHERE r.pension.pensionId IN :pensionIds AND r.price IS NOT NULL " +
            "GROUP BY r.pension.pensionId")
    List<Object[]> findAveragePricesByPensionIds(@Param("pensionIds") List<Long> pensionIds);

}
