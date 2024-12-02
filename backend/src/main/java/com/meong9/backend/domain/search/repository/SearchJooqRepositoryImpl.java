package com.meong9.backend.domain.search.repository;

import com.meong9.backend.domain.search.dto.SearchPensionDto;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.meong9.backend.jooq.generated.Tables.*;

@Repository
@RequiredArgsConstructor
public class SearchJooqRepositoryImpl implements SearchJooqRepository {

    private final DSLContext dsl;

    @Override
    public Slice<SearchPlaceDto> searchPlaces(List<Long> filteredPlaceIds, List<Long> categoryIds, String sizeCode, String typeCode, Pageable pageable, Long memberId) {
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
                        PLACE.PLACE_ID.in(filteredPlaceIds)
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
    public Slice<SearchPensionDto> searchPensions(List<Long> filteredPensionIds, String startDate, String endDate, String sizeCode, String typeCode, Pageable pageable, Long memberId) {
        int offset = (int) pageable.getOffset();
        int pageSize = pageable.getPageSize();

        // jOOQ 쿼리
        List<SearchPensionDto> pensions = dsl.select(
                        PENSION.PENSION_ID.as("pensionId"),
                        PENSION.PENSION_NAME.as("pensionName"),
                        getAddressFieldForPension(typeCode).as("address"),
                        DSL.inline("펜션").as("placeType"),
                        PENSION.REVIEW_AVG.as("reviewAvg"),
                        PENSION.REVIEW_COUNT.as("reviewCount"),
                        PENSION.ENTER_PET_SIZE.as("weightLimit"),
                        DSL.min(ROOM.PRICE).as("lowestPrice"), // 최소 가격
                        DSL.max(ROOM.GUEST_COUNT).as("guestCount"), // 최대 게스트 수
                        DSL.max(ROOM.PET_COUNT).as("petCount"), // 최대 반려동물 수
                        PENSION.START_TIME.as("startTime"),
                        PENSION.END_TIME.as("endTime"),
                        getLikeStatusFieldForPension(memberId),
                        DSL.when(
                                DSL.exists(
                                        DSL.selectOne()
                                                .from(ROOM)
                                                .join(ROOM_AVAILABILITY).on(ROOM.ROOM_ID.eq(ROOM_AVAILABILITY.ROOM_ID))
                                                .where(ROOM.PENSION_ID.eq(PENSION.PENSION_ID))
                                                .and(ROOM_AVAILABILITY.DATE.between(
                                                        DSL.val(LocalDate.parse(startDate)),
                                                        DSL.val(LocalDate.parse(endDate))
                                                ))
                                                .and(ROOM_AVAILABILITY.IS_AVAILABLE.isTrue())
                                ),
                                DSL.inline(1) // 예약 가능
                        ).otherwise(DSL.inline(0)).as("isAvailable") // 예약 가능 여부 플래그
                )
                .from(PENSION)
                .leftJoin(ROOM).on(PENSION.PENSION_ID.eq(ROOM.PENSION_ID))
                .where(
                        PENSION.PENSION_ID.in(filteredPensionIds) // 필터링된 펜션 ID
                )
                .groupBy( // 모든 SELECT 열을 GROUP BY에 추가
                        PENSION.PENSION_ID,
                        PENSION.PENSION_NAME,
                        getAddressFieldForPension(typeCode),
                        PENSION.REVIEW_AVG,
                        PENSION.REVIEW_COUNT,
                        PENSION.ENTER_PET_SIZE,
                        PENSION.START_TIME,
                        PENSION.END_TIME
                )
                .orderBy(DSL.field("isAvailable").desc(), PENSION.REVIEW_COUNT.desc()) // 예약 가능 여부 > 리뷰 평균 기준 정렬
                .limit(pageSize)
                .offset(offset)
                .fetchInto(SearchPensionDto.class); // DTO로 매핑

        pensions.forEach(pension -> {
            List<String> tags = getTagsForPension(pension.getPensionId()); // 태그 조회
            pension.setTags(tags);

            List<String> images = getImagesForPension(pension.getPensionId()); // 이미지 조회
            pension.setImages(images);
        });

        boolean hasNext = pensions.size() == pageSize;

        return new SliceImpl<>(pensions, pageable, hasNext);
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

    @Override
    public List<Long> findPensionIdsBySearchWord(String searchWord) {
        // 검색어를 단어별로 나누어 배열에 담기
        String[] searchWords = searchWord.split(" ");

        // pension 테이블에서 검색
        var pensionQuery = dsl
                .select(PENSION.PENSION_ID)
                .from(PENSION)
                .where(
                        Arrays.stream(searchWords)
                                .map(word -> DSL.condition("MATCH(pension_name, info) AGAINST (? IN BOOLEAN MODE)", word + "*"))
                                .reduce(DSL.noCondition(), DSL::or)
                );

        // address 테이블에서 검색
        var addressQuery = dsl
                .select(PLC_PEN_ADDRESS.PLC_PEN_ID)
                .from(ADDRESS)
                .join(PLC_PEN_ADDRESS).on(ADDRESS.ADDRESS_ID.eq(PLC_PEN_ADDRESS.ADDRESS_ID))
                .where(
                        Arrays.stream(searchWords)
                                .map(word -> DSL.condition("MATCH(address, province, city_district, subdistrict) AGAINST (? IN BOOLEAN MODE)", word + "*"))
                                .reduce(DSL.noCondition(), DSL::or)
                );

        // union으로 합치기 (or과 비교해 볼 필요 있음)
        return dsl.selectDistinct(DSL.field("pension_id", Long.class))
                .from(pensionQuery.union(addressQuery).asTable("combined_results"))
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

    private Field<String> getAddressFieldForPension(String typeCode) {
        return dsl.select(ADDRESS.ADDRESS_)
                .from(PLC_PEN_ADDRESS)
                .join(ADDRESS).on(PLC_PEN_ADDRESS.ADDRESS_ID.eq(ADDRESS.ADDRESS_ID))
                .where(PLC_PEN_ADDRESS.PLC_PEN_ID.eq(PENSION.PENSION_ID))
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

    private List<String> getTagsForPension(Long pensionId) {
        return dsl.select(TAG.TAG_NAME)
                .from(PENSION_TAG)
                .join(TAG).on(PENSION_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(PENSION_TAG.PENSION_ID.eq(pensionId))
                .fetchInto(String.class);
    }

    private List<String> getImages(Long placeId) {
        return dsl.select(MEDIA_FILE.FILE_URL)
                .from(PLACE_FILE)
                .join(MEDIA_FILE).on(PLACE_FILE.MEDIA_FILE_ID.eq(MEDIA_FILE.MEDIA_FILE_ID))
                .where(PLACE_FILE.PLACE_ID.eq(placeId))
                .fetchInto(String.class);
    }

    private List<String> getImagesForPension(Long pensionId) {
        return dsl.select(MEDIA_FILE.FILE_URL)
                .from(PENSION_FILE)
                .join(MEDIA_FILE).on(PENSION_FILE.MEDIA_FILE_ID.eq(MEDIA_FILE.MEDIA_FILE_ID))
                .where(PENSION_FILE.PENSION_ID.eq(pensionId))
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

    private Field<Boolean> getLikeStatusFieldForPension(Long memberId) {
        if (memberId == null) {
            return DSL.inline(false).as("likeStatus");
        }

        return DSL.when(
                        DSL.exists(
                                DSL.selectOne()
                                        .from(LIKES)
                                        .where(LIKES.MEMBER_ID.eq(memberId))
                                        .and(LIKES.PENSION_ID.eq(PENSION.PENSION_ID))
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
