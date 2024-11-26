package com.meong9.backend.domain.like.service;

import com.meong9.backend.domain.like.entity.PensionLike;
import com.meong9.backend.domain.like.entity.PlaceLike;
import com.meong9.backend.domain.like.repository.LikeRepository;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final PlaceRepository placeRepository;
    private final PensionRepository pensionRepository;

    @Transactional
    public String togglePlaceLike(Member member, Long placeId) {
        Place place=placeRepository.findById(placeId).orElseThrow(()->NotFoundException.entityNotFound("장소"));
        place.increaseLikeCount();
        return likeRepository.findByMemberAndPlace(member, place)
                .map(like -> {
                    likeRepository.delete(like);
                    return "찜하기가 취소되었습니다.";
                })
                .orElseGet(() -> {
                    likeRepository.save(new PlaceLike(member, place));
                    return "찜하기가 등록되었습니다.";
                });
    }

    @Transactional
    public String togglePensionLike(Member member, Long pensionId) {
        Pension pension=pensionRepository.findById(pensionId).orElseThrow(()->NotFoundException.entityNotFound("장소"));
        pension.increaseLikeCount();
        return likeRepository.findByMemberAndPension(member, pension)
                .map(like -> {
                    likeRepository.delete(like);
                    return "찜하기가 취소되었습니다.";
                })
                .orElseGet(() -> {
                    likeRepository.save(new PensionLike(member, pension));
                    return "찜하기가 등록되었습니다.";
                });
    }
}
