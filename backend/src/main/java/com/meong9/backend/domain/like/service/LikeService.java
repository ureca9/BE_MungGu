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
                    place.decreaseLikeCount();
                    likeRepository.delete(like);
                    return "찜하기가 취소되었습니다.";
                })
                .orElseGet(() -> {
                    place.increaseLikeCount();
                    likeRepository.save(new PlaceLike(member, place));
                    return "찜하기가 등록되었습니다.";
                });
    }

    @Transactional
    public String togglePensionLike(Member member, Long pensionId) {
        Pension pension=pensionRepository.findById(pensionId).orElseThrow(()->NotFoundException.entityNotFound("펜션"));
        return likeRepository.findByMemberAndPension(member, pension)
                .map(like -> {
                    pension.decreaseLikeCount();
                    likeRepository.delete(like);
                    return "찜하기가 취소되었습니다.";
                })
                .orElseGet(() -> {
                    pension.increaseLikeCount();
                    likeRepository.save(new PensionLike(member, pension));
                    return "찜하기가 등록되었습니다.";
                });
    }

    // Place 즐겨찾기 여부 확인
    @Transactional(readOnly = true)
    public boolean isPlaceLikedByMember(Member member, Long placeId) {
        return likeRepository.existsByMemberAndPlaceId(member, placeId);
    }

    // Pension 즐겨찾기 여부 확인
    @Transactional(readOnly = true)
    public boolean isPensionLikedByMember(Member member, Long pensionId) {
        return likeRepository.existsByMemberAndPensionId(member, pensionId);
    }
}
