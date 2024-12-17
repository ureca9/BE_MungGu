package com.meong9.backend.domain.member.service;

import com.meong9.backend.domain.member.dto.*;
import com.meong9.backend.domain.member.entity.FavoriteRegion;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.entity.PlcFavCategory;
import com.meong9.backend.domain.member.repository.FavoriteRegionRepository;
import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.domain.member.repository.PlcFavCategoryRepository;
import com.meong9.backend.domain.place.entity.PlcCategory;
import com.meong9.backend.domain.place.repository.PlcCategoryRepository;
import com.meong9.backend.domain.puppy.entity.Puppy;
import com.meong9.backend.domain.puppy.repository.PuppyRepository;
import com.meong9.backend.global.auth.jwt.JwtProvider;
import com.meong9.backend.global.auth.refreshtoken.RefreshToken;
import com.meong9.backend.global.auth.refreshtoken.RefreshTokenService;
import com.meong9.backend.global.entity.Region;
import com.meong9.backend.global.exception.AuthenticationException;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import com.meong9.backend.global.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MemberService")
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final PlcCategoryRepository plcCategoryRepository;
    private final PlcFavCategoryRepository plcFavCategoryRepository;
    private final FavoriteRegionRepository favoriteRegionRepository;
    private final RegionRepository regionRepository;
    private final MediaFileService mediaFileService;
    private final PuppyRepository puppyRepository;

    /**
     * refresh token 사용하여 access token 재발급하는 서비스 메서드
     */
    @Transactional
    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null) {
            throw AuthenticationException.noRefreshToken();
        }

        jwtProvider.validateToken(refreshToken);
        String email = jwtProvider.getSubjectFromToken(refreshToken);

        RefreshToken storedRefreshToken = refreshTokenService.getRefreshToken(email)
                .orElseThrow(() -> NotFoundException.entityNotFound("리프레시 토큰"));

        if (!refreshTokenService.validateRefreshToken(email, refreshToken.substring(7))) {
            refreshTokenService.removeRefreshToken(storedRefreshToken);
            throw AuthenticationException.unauthenticatedToken(refreshToken);
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> NotFoundException.entityNotFound("멤버"));

        return jwtProvider.createAccessToken(email, member.getRoleCode());
    }

    /**
     * refresh token의 Max age를 0으로 만들어 로그아웃 시키는 메서드
     */
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null) {
            throw AuthenticationException.noRefreshToken();
        }

        jwtProvider.validateToken(refreshToken);
        refreshTokenService.removeRefreshTokenByKeyEmail(jwtProvider.getSubjectFromToken(refreshToken));
    }

    /**
     * 사용자의 선호 시설을 저장하는 서비스 메서드
     */
    @Transactional
    public void insertPreferredPlaces(InterestDto dto, Member member) {
        if (dto.getPlaces() == null || dto.getPlaces().isEmpty()) return;

        Set<String> newCategoryNames = dto.getPlaces();
        plcFavCategoryRepository.deleteByMemberId(member.getMemberId());

        List<PlcCategory> categoriesToInsert = plcCategoryRepository.findAllByNameIn(newCategoryNames);

        if (categoriesToInsert.size() != newCategoryNames.size()) {
            throw NotFoundException.entityNotFound("카테고리");
        }

        List<Object[]> batchParams = categoriesToInsert.stream()
                .map(category -> new Object[]{member.getMemberId(), category.getPlcCategoryId()})
                .collect(Collectors.toList());

        plcFavCategoryRepository.batchInsert(batchParams);
    }

    /**
     * 사용자의 선호 지역을 저장하는 서비스 메서드
     */
    @Transactional
    public void insertPreferredRegions(RegionDto dto, Member member) {
        if (dto.getRegions() == null || dto.getRegions().isEmpty()) return;

        Set<String> newRegionsNames = dto.getRegions();
        favoriteRegionRepository.deleteByMemberId(member.getMemberId());

        List<Region> regionsToInsert = regionRepository.findAllByNameIn(newRegionsNames);

        if (regionsToInsert.size() != newRegionsNames.size()) {
            throw NotFoundException.entityNotFound("지역");
        }

        List<Object[]> batchParams = regionsToInsert.stream()
                .map(region -> new Object[]{member.getMemberId(), region.getRegionId()})
                .collect(Collectors.toList());

        favoriteRegionRepository.batchInsert(batchParams);
    }

    /**
     * 사용자 정보를 등록하는 서비스 메서드
     */
    @Transactional
    public void insertMemberInfo(MultipartFile profileImage, MemberInfoDto dto, Member member) throws IOException {
        updateMember(profileImage, dto, member);
        memberRepository.save(member);
    }

//    @Transactional
//    public void insertMemberInfo(String fileKey,MemberInfoDto dto, Member member) throws IOException {
//        updateMember(fileKey, dto, member);
//
//        memberRepository.save(member);
//    }

    /**
     * 프로필 이미지를 삭제하는 서비스 메서드
     */
    @Transactional
    public void deleteProfileImage(Member member) {
        mediaFileService.deleteProfileImage(member.getMemberId(),"Mprofile/","_profile.jpg");
        member.setProfileImage(null);
    }

    /**
     * 닉네임 중복을 확인하는 서비스 메서드
     */
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }

    /**
     * 마이페이지를 조회하는 서비스 메서드
     */
    @Transactional(readOnly = true)
    public MypageDto getMyPage(Member member) {
        List<Puppy> puppies = puppyRepository.findByMemberIdWithPuppyProfileImage(member.getMemberId());
        Member foundMember = memberRepository.findMemberWithProfileImage(member.getMemberId()).orElseThrow();
        List<MypagePuppyDto> puppyList = puppies.stream()
                .map(puppy -> MypagePuppyDto.builder()
                        .puppyId(puppy.getPuppyId())
                        .puppyName(puppy.getName())
                        .puppyImageUrl(
                                puppy.getProfileImage() != null ?
                                        mediaFileService.getResizeBucketUrl(puppy.getProfileImage().getFileKey()) : null
                        )
                        .build())
                .toList();
        return MypageDto.builder()
                .memberId(foundMember.getMemberId())
                .nickname(foundMember.getNickname())
                .profileImageUrl(mediaFileService.getResizeBucketUrl(foundMember.getProfileImage().getFileKey()))
                .puppyList(puppyList)
                .build();
    }

    /**
     * 마이페이지 상세 조회 메서드
     */
    @Transactional(readOnly = true)
    public MyPageDetailDto getMyPageDetail(Member member) {
        Member foundMember = memberRepository.findMemberWithProfileImage(member.getMemberId()).orElseThrow();
        return MyPageDetailDto.builder()
                .email(foundMember.getEmail())
                .name(foundMember.getName())
                .nickname(foundMember.getNickname())
                .phone(foundMember.getPhone())
                .profileImageUrl(mediaFileService.getResizeBucketUrl(foundMember.getProfileImage().getFileKey()))
                .build();
    }

    /**
     * 마이페이지 수정 메서드
     */
    @Transactional
    public UpdateMyPageResponseDto updateMyPage(MultipartFile profileImage, MemberInfoDto dto, Member member) throws IOException {
        updateMember(profileImage, dto, member);

        Member savedMember = memberRepository.save(member);
        return UpdateMyPageResponseDto.builder()
                .name(savedMember.getName())
                .phone(savedMember.getPhone())
                .nickname(savedMember.getNickname())
                .profileImageUrl(savedMember.getProfileImage().getFileUrl())
                .build();
    }

//    @Transactional
//    public UpdateMyPageResponseDto updateMyPage(String fileKey, MemberInfoDto dto, Member member) throws IOException {
//        updateMember(fileKey, dto, member);
//
//        Member savedMember = memberRepository.save(member);
//        return UpdateMyPageResponseDto.builder()
//                .name(savedMember.getName())
//                .phone(savedMember.getPhone())
//                .nickname(savedMember.getNickname())
//                .profileImageUrl(savedMember.getProfileImage().getFileUrl())
//                .build();
//    }

    private void updateMember(MultipartFile profileImage, MemberInfoDto dto, Member member) throws IOException {
        member.setName(dto.getName().trim());
        member.setPhone(dto.getPhone().trim());
        member.setNickname(dto.getNickname().trim());

        if (profileImage != null) {
            mediaFileService.uploadProfileImage(profileImage, member.getMemberId(),"Mprofile/","_profile.jpg");
        }
    }

//    private void updateMember(String fileKey, MemberInfoDto dto, Member member) throws IOException {
//        member.setName(dto.getName().trim());
//        member.setPhone(dto.getPhone().trim());
//        member.setNickname(dto.getNickname().trim());
//        member.setProfileImage(mediaFileService.registerFileKey(fileKey));
//    }

    /**
     * 선호 지역 조회 메서드
     */
    @Transactional(readOnly = true)
    public RegionDto getPreferredRegions(Member member) {
        List<FavoriteRegion> favRegionList = favoriteRegionRepository.findByMemberId(member.getMemberId());
        return new RegionDto(favRegionList.stream()
                .map(favoriteRegion -> favoriteRegion.getRegion().getName())
                .collect(Collectors.toSet()));
    }

    /**
     * 선호 시설 조회 메서드
     */
    @Transactional(readOnly = true)
    public InterestDto getPreferredPlaces(Member member) {
        List<PlcFavCategory> favCategoryList = plcFavCategoryRepository.findByMemberId(member.getMemberId());
        return new InterestDto(favCategoryList.stream()
                .map(favoritePlace -> favoritePlace.getPlcCategory().getName())
                .collect(Collectors.toSet()));
    }
}
