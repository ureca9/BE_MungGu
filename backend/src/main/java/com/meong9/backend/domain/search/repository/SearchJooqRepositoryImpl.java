package com.meong9.backend.domain.search.repository;

import com.meong9.backend.domain.search.dto.SearchPlaceDto;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
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
                        PLC_CATEGORY.PLC_CATEGORY_NAME.as("placeType"), // placeType
                        PLACE.REVIEW_AVG, // reviewAverage
                        PLACE.REVIEW_COUNT, // reviewCount
                        PLACE.ENTER_PET_SIZE.as("weightLimit"), // weightLimit
                        PLACE.BUSINESS_HOUR, // businessHour
                        getLikeStatusField(memberId) // boolean likeStatus
                )
                .from(PLACE)
                .join(PLC_CATEGORY).on(PLACE.PLC_CATEGORY_ID.eq(PLC_CATEGORY.PLC_CATEGORY_ID))
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
            place.setTags(tags);

            List<String> images = getImages(place.getPlaceId()); // 이미지 조회
            place.setImages(images);
        });

        boolean hasNext = places.size() == pageSize;

        return new SliceImpl<>(places, pageable, hasNext);
    }

    @Override
    public List<Long> findPlaceIdsBySearchWord(String searchWord) {
        // 검색어를 단어별로 나누어 배열에 담기
        String[] searchWords = searchWord.split(" ");

        return dsl
                .selectDistinct(DSL.field("p.place_id", Long.class))
                .from(
                        dsl.select(PLACE.PLACE_ID)
                                .from(PLACE)
                                .where(
                                        Arrays.stream(searchWords)
                                                .map(word -> DSL.condition(
                                                        "MATCH(place_name, plc_description) AGAINST (? IN BOOLEAN MODE)", word + "*"
                                                ))
                                                .reduce(DSL.noCondition(), DSL::or)
                                )
                                .asTable("p")
                )
                .join(PLC_PEN_ADDRESS).on(DSL.field("p.place_id").eq(PLC_PEN_ADDRESS.PLC_PEN_ID))
                .join(
                        dsl.select(ADDRESS.ADDRESS_ID)
                                .from(ADDRESS)
                                .where(
                                        Arrays.stream(searchWords)
                                                .map(word -> DSL.condition(
                                                        "MATCH(address, province, city_district, subdistrict) AGAINST (? IN BOOLEAN MODE)", word + "*"
                                                ))
                                                .reduce(DSL.noCondition(), DSL::or)
                                )
                                .asTable("a")
                ).on(PLC_PEN_ADDRESS.ADDRESS_ID.eq(DSL.field("a.address_id", Long.class)))
                .fetchInto(Long.class);
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

    private Condition getWeightCondition(String sizeCode) {
        return PLACE.ENTER_PET_SIZE.eq("030")
                .or(PLACE.ENTER_PET_SIZE.eq("020").and(DSL.val(sizeCode).in("020", "010")))
                .or(PLACE.ENTER_PET_SIZE.eq("010").and(DSL.val(sizeCode).eq("010")));
    }

}
