package com.meong9.backend.domain.map.service;

import com.meong9.backend.domain.like.repository.LikeRepository;
import com.meong9.backend.domain.map.dto.MapLikePointDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {

    private LikeRepository likeRepository;

    // 찜 마커
    @Transactional(readOnly = true)
    public List<MapLikePointDto> getMapLikePoints() {
//        likeRepository.findAll
        return null;
    }
}
