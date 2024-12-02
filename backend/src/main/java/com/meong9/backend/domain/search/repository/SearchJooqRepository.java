package com.meong9.backend.domain.search.repository;

import com.meong9.backend.domain.search.dto.SearchPlaceDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface SearchJooqRepository {
    Slice<SearchPlaceDto> searchPlaces(List<Long> regionIds, List<Long> categoryIds, String sizeCode, String typeCode, Pageable pageable, Long memberId);

    List<Long> findPlaceIdsBySearchWord(String searchWord);
}
