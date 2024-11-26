package com.meong9.backend.domain.puppy.service;

import com.meong9.backend.domain.puppy.entity.Breed;
import com.meong9.backend.domain.puppy.repository.BreedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BreedService {

    private final BreedRepository breedRepository;

    public Breed findBreedById(Long breedId) {
        return breedRepository.findById(breedId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 품종 ID입니다."));
    }

    /**
     * Breed 테이블의 모든 데이터 조회
     * @return 모든 Breed 엔티티 리스트
     */
    public List<Breed> findAllBreeds() {
        return breedRepository.findAll();
    }
}