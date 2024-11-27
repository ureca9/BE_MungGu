package com.meong9.backend.domain.puppy.service;

import com.meong9.backend.domain.puppy.entity.Breed;
import com.meong9.backend.domain.puppy.repository.BreedRepository;
import com.meong9.backend.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BreedService {

    private final BreedRepository breedRepository;

    @Transactional(readOnly = true)
    public Breed findBreedById(Long breedId) {
        return breedRepository.findById(breedId)
                .orElseThrow(BadRequestException::invalidPuppyIdFormat);
    }

    /**
     * Breed 테이블의 모든 데이터 조회
     * @return 모든 Breed 엔티티 리스트
     */
    @Transactional(readOnly = true)
    public List<Breed> findAllBreeds() {
        return breedRepository.findAll();
    }
}