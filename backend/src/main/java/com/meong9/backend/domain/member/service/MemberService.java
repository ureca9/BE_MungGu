package com.meong9.backend.domain.member.service;

import com.meong9.backend.domain.member.dto.InterestDto;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.domain.member.repository.PlcFavCategoryJdbcRepositoryImpl;
import com.meong9.backend.domain.member.repository.PlcFavCategoryRepository;
import com.meong9.backend.domain.place.entity.PlcCategory;
import com.meong9.backend.domain.place.repository.PlcCategoryRepository;
import com.meong9.backend.global.auth.refreshtoken.RefreshToken;
import com.meong9.backend.global.auth.refreshtoken.RefreshTokenService;
import com.meong9.backend.global.auth.utils.JwtProvider;
import com.meong9.backend.global.exception.AuthenticationException;
import com.meong9.backend.global.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "MemberService")
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final PlcCategoryRepository plcCategoryRepository;
    private final PlcFavCategoryRepository plcFavCategoryRepository;
    private final PlcFavCategoryJdbcRepositoryImpl plcFavCategoryRepositoryImpl;

    /**
     * refresh token 사용하여 access token 재발급하는 서비스 메서드
     */
    public String refreshAccessToken(String refreshToken) {
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
     * 사용자의 선호 시설을 저장하는 서비스 메서드
     */
    public void insertInterests(InterestDto dto, Member member) {
        if (dto.getPlaces() == null || dto.getPlaces().isEmpty()) return;

        Set<String> newCategoryNames = dto.getPlaces();
        plcFavCategoryRepository.deleteByMemberId(member.getMemberId());

        List<PlcCategory> categoriesToInsert = plcCategoryRepository.findAllByNameIn(newCategoryNames);

        if (categoriesToInsert.size() != newCategoryNames.size()) {
            throw NotFoundException.entityNotFound("일부 카테고리");
        }

        List<Object[]> batchParams = categoriesToInsert.stream()
                .map(category -> new Object[]{member.getMemberId(), category.getPlcCategoryId()})
                .collect(Collectors.toList());

        plcFavCategoryRepositoryImpl.batchInsert(batchParams);
    }
}
