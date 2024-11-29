package com.meong9.backend.domain.search.service;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.puppy.entity.Puppy;
import com.meong9.backend.domain.puppy.repository.PuppyRepository;
import com.meong9.backend.domain.search.dto.PuppiesForSearchDto;
import com.meong9.backend.domain.search.dto.PuppiesWithWeightDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class SearchService {
    private final PuppyRepository puppyRepository;

    public SearchService(PuppyRepository puppyRepository) {
        this.puppyRepository = puppyRepository;
    }

    public PuppiesForSearchDto getPuppiesForSearch(Member member) {
        List<Puppy> puppies = puppyRepository.findByMemberIdWithPuppyProfileImage(member.getMemberId());
        List<PuppiesWithWeightDto> puppiesWithWeightDto = puppies.stream()
                .map(puppy -> PuppiesWithWeightDto.builder()
                        .puppyId(puppy.getPuppyId())
                        .puppyWeight(puppy.getWeight())
                        .puppyName(puppy.getName())
                        .puppyImageUrl(puppy.getProfileImage().getFileUrl())
                        .build())
                .toList();
        return new PuppiesForSearchDto(member.getMemberId(), puppiesWithWeightDto);
    }
}
