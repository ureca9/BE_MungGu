package com.meong9.backend.domain.search.repository;

import com.meong9.backend.domain.search.dto.SearchPensionDto;
import com.meong9.backend.domain.search.dto.SearchPlaceDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface SearchJooqRepository {
    List<SearchPlaceDto> searchPlaces(List<Long> regionIds, List<Long> categoryIds, String sizeCode, String typeCode, Long memberId);
    List<SearchPensionDto> searchPensions(List<Long> filteredPensionIds, String startDate, String endDate, String sizeCode, String typeCode, Long memberId);

    Slice<Long> findPlaceIdsBySearchWord(String searchWord, Pageable pageable);
    Slice<Long> findPensionIdsBySearchWord(String searchWord, Pageable pageable);

}
