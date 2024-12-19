package com.meong9.backend.global.blacklist.repository;

import com.meong9.backend.global.blacklist.entity.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BlackListRepository extends JpaRepository<BlackList, Long> {

    List<BlackList> findAllByLockedUntilBefore(LocalDateTime now);
}
