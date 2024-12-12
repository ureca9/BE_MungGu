package com.meong9.backend.domain.search.repository;

import com.meong9.backend.domain.search.dto.SearchPensionDto;
import com.meong9.backend.domain.search.dto.SearchPlaceDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDate;
import java.util.List;

public interface SearchJooqRepository {
    List<SearchPlaceDto> searchPlaces(List<Long> filteredPlaceIds, List<Long> categoryIds, String typeCode, Long memberId);
    List<SearchPensionDto> searchPensions(List<Long> filteredPensionIds, String startDate, String endDate, String sizeCode, String typeCode, Long memberId);

    Slice<Long> findPlaceIdsBySearchWordForMap(String searchWord, Pageable pageable);
    Slice<Long> findPensionIdsBySearchWordForMap(String searchWord, Pageable pageable);

    List<Long> findPensionIdsBySearchWord(String searchWord);
    List<Long> findPlaceIdsBySearchWord(String searchWord);

    Slice<Long> findPlaceIdsMatchWithCategoryIds(List<Long> firstFilteredPlaceIds, List<Long> categoryIds, String sizeCode, Pageable pageable);
    Slice<Long> findPensionIdsIsAvailable(List<Long> firstFilteredPensionIds, String sizeCode, LocalDate start, LocalDate end, Pageable pageable);
}
