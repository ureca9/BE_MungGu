package com.meong9.backend.domain.search.repository;

import com.meong9.backend.domain.search.dto.SearchPlaceDto;
import com.meong9.backend.jooq.generated.tables.Address;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.meong9.backend.jooq.generated.Tables.*;

@Repository
@RequiredArgsConstructor
public class SearchJooqRepositoryImpl implements SearchJooqRepository {

    private final DSLContext dsl;

    @Override
    public Slice<SearchPlaceDto> searchPlaces(List<Long> placeIdsByRegion, List<Long> categoryIds, String sizeCode, String typeCode, Pageable pageable, Long memberId) {
        int offset = (int) pageable.getOffset();
        int pageSize = pageable.getPageSize();

        List<SearchPlaceDto> places = dsl.select(
                        PLACE.PLACE_ID, // placeId
                        PLACE.PLACE_NAME.as("placeName"), // placeName
                        getAddressField(typeCode).as("address"), // address
                        PLACE.PLC_CATEGORY_ID.as("placeType"), // placeType
                        PLACE.REVIEW_AVG, // reviewAverage
                        PLACE.REVIEW_COUNT, // reviewCount
                        PLACE.ENTER_PET_SIZE.as("weightLimit"), // weightLimit
                        PLACE.BUSINESS_HOUR, // businessHour
                        getLikeStatusField(memberId) // boolean likeStatus
                )
                .from(PLACE)
                .where(
                        PLACE.PLACE_ID.in(placeIdsByRegion)
                                .and(PLACE.PLC_CATEGORY_ID.in(categoryIds))
                                .and(getWeightCondition(sizeCode))
                )
                .limit(pageSize)
                .offset(offset)
                .fetchInto(SearchPlaceDto.class);

        places.forEach(place -> {
            List<String> tags = getTags(place.getPlaceId()); // 태그 조회
            place.setTags(tags); // DTO에 태그 리스트 설정

            List<String> images = getImages(place.getPlaceId()); // 이미지 조회
            place.setImages(images); // DTO에 이미지 리스트 설정
        });

        boolean hasNext = places.size() == pageSize;

        return new SliceImpl<>(places, pageable, hasNext);
    }

    private Field<String> getAddressField(String typeCode) {
        return dsl.select(ADDRESS.ADDRESS_)
                .from(PLC_PEN_ADDRESS)
                .join(ADDRESS).on(PLC_PEN_ADDRESS.ADDRESS_ID.eq(ADDRESS.ADDRESS_ID))
                .where(PLC_PEN_ADDRESS.PLC_PEN_ID.eq(PLACE.PLACE_ID))
                .and(PLC_PEN_ADDRESS.TYPE.eq(typeCode))
                .asField();
    }

    private List<String> getTags(Long placeId) {
        return dsl.select(TAG.TAG_NAME)
                .from(PLACE_TAG)
                .join(TAG).on(PLACE_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(PLACE_TAG.PLACE_ID.eq(placeId))
                .fetchInto(String.class);
    }

    private List<String> getImages(Long placeId) {
        return dsl.select(MEDIA_FILE.FILE_URL)
                .from(PLACE_FILE)
                .join(MEDIA_FILE).on(PLACE_FILE.MEDIA_FILE_ID.eq(MEDIA_FILE.MEDIA_FILE_ID))
                .where(PLACE_FILE.PLACE_ID.eq(placeId))
                .fetchInto(String.class);
    }

    private Field<Boolean> getLikeStatusField(Long memberId) {
        if (memberId == null) {
            return DSL.inline(false).as("likeStatus");
        }

        return DSL.when(
                        DSL.exists(
                                DSL.selectOne()
                                        .from(LIKES)
                                        .where(LIKES.MEMBER_ID.eq(memberId))
                                        .and(LIKES.PLACE_ID.eq(PLACE.PLACE_ID))
                        ),
                        DSL.inline(true)
                )
                .otherwise(DSL.inline(false))
                .as("likeStatus");
    }

//    private SelectConditionStep<Record1<Long>> getFilteredPlaceIds(List<Long> regionIds, String typeCode) {
//        return DSL.select(PLC_PEN_ADDRESS.PLC_PEN_ID)
//                .from(PLC_PEN_ADDRESS)
//                .join(ADDRESS).on(PLC_PEN_ADDRESS.PLC_PEN_ID.eq(ADDRESS.ADDRESS_ID))
//                .where(ADDRESS.REGION_ID.in(regionIds))
//                .and(PLC_PEN_ADDRESS.TYPE.eq(typeCode));
//    }

    private Condition getWeightCondition(String sizeCode) {
        return PLACE.ENTER_PET_SIZE.eq("030")
                .or(PLACE.ENTER_PET_SIZE.eq("020").and(DSL.val(sizeCode).in("020", "010")))
                .or(PLACE.ENTER_PET_SIZE.eq("010").and(DSL.val(sizeCode).eq("010")));
    }

}
