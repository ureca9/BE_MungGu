package com.meong9.backend.domain.map.service;

import com.meong9.backend.domain.like.entity.Like;
import com.meong9.backend.domain.like.entity.PensionLike;
import com.meong9.backend.domain.like.entity.PlaceLike;
import com.meong9.backend.domain.like.repository.LikeRepository;
import com.meong9.backend.domain.map.dto.MapLikePointDto;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.jooq.generated.tables.Likes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {

    private final LikeRepository likeRepository;

    // 찜 마커
    @Transactional(readOnly = true)
    public List<MapLikePointDto> getMapLikePoints(Member member) {
        List<PensionLike> pensionLikes = likeRepository.findAllPensionLikes(member);
        List<PlaceLike> placeLikes = likeRepository.findAllPlaceLikes(member);

        // dto 매핑
        List<MapLikePointDto> mapLikePointList = new ArrayList<>();
        for(PensionLike pensionLike : pensionLikes){
            MapLikePointDto mapLikePointDto = MapLikePointDto.builder()
                    .id(pensionLike.getPension().getPensionId())
                    .type("펜션")
                    .name(pensionLike.getPension().getName())
                    .latitude(pensionLike.getPension().getLatitude())
                    .longitude(pensionLike.getPension().getLongitude())
                    .build();

            mapLikePointList.add(mapLikePointDto);
        }

        for(PlaceLike placeLike : placeLikes){
            MapLikePointDto mapLikePointDto = MapLikePointDto.builder()
                    .id(placeLike.getPlace().getPlaceId())
                    .type("시설")
                    .name(placeLike.getPlace().getName())
                    .latitude(placeLike.getPlace().getLatitude())
                    .longitude(placeLike.getPlace().getLongitude())
                    .build();

            mapLikePointList.add(mapLikePointDto);
        }

        return mapLikePointList;
    }
}
