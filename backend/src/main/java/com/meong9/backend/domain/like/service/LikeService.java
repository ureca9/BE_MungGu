package com.meong9.backend.domain.like.service;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.like.entity.PensionLike;
import com.meong9.backend.domain.like.entity.PlaceLike;
import com.meong9.backend.domain.like.repository.LikeRepository;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.recommendation.recommendation.dto.RecommendationDto;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.utils.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final PlaceRepository placeRepository;
    private final PensionRepository pensionRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;

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

    // 좋아요 가장 많은 펜션
    public List<RecommendationDto> getTopLikedPensions(int count) {
        PageRequest pageRequest = PageRequest.of(0, count);

        // 좋아요가 많은 펜션 ID 가져오기
        Page<Long> pensionIdPage = likeRepository.findTopPensionIds(pageRequest);
        List<Long> pensionIdList = pensionIdPage.getContent();

        if (pensionIdList.isEmpty()) {
            throw NotFoundException.entityNotFound("추천된 펜션");
        }

        // 펜션 및 주소 가져오기
        List<Pension> pensions = pensionRepository.findAllDataByIds(pensionIdList);
        List<PlcPenAddress> plcPenAddresses = plcPenAddressRepository.findAddressesByIdsAndType(pensionIdList, "020");

        // 주소 매핑
        Map<Long, String> addressMap = plcPenAddresses.stream()
                .collect(Collectors.toMap(
                        PlcPenAddress::getPlcPenId, // 장소 ID를 키로 사용
                        AddressMapper::formatAddress
                ));

        // RecommendationDto 생성
        List<RecommendationDto> recommendationDtos = new ArrayList<>();
        for (Pension pension : pensions) {
            String formattedAddress = addressMap.get(pension.getPensionId());
            recommendationDtos.add(RecommendationDto.createdRecommendationDto(pension, formattedAddress));
        }

        return recommendationDtos;
    }

}
