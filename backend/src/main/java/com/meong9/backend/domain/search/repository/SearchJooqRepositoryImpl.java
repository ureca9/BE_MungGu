package com.meong9.backend.domain.search.repository;

import com.meong9.backend.domain.search.dto.SearchPensionDto;
import com.meong9.backend.domain.search.dto.SearchPlaceDto;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.meong9.backend.jooq.generated.Tables.*;

@Repository
@RequiredArgsConstructor
public class SearchJooqRepositoryImpl implements SearchJooqRepository {

    private final DSLContext dsl;

    @Override
    public Slice<SearchPlaceDto> searchPlaces(List<Long> filteredPlaceIds, List<Long> categoryIds, String sizeCode, String typeCode, Long memberId, Pageable pageable) {

        // 1. 1차 필터링된 장소ID에 더해, 2차로 카테고리 필터링, 3차로 무게 필터링을 마친 filted_placesid_field 생성
        var filteredPlaceSubquery = dsl.select(PLACE.PLACE_ID)
                .from(PLACE)
                .where(PLACE.PLACE_ID.in(filteredPlaceIds))
                .and(PLACE.PLC_CATEGORY_ID.in(categoryIds))
                .and(getWeightCondition(sizeCode))
                .limit(pageable.getPageSize() + 1)
                .offset((int) pageable.getOffset())
                .asTable("filtered_places");

        Field<Long> filteredPlaceIdField = DSL.field("filtered_places.place_id", Long.class);

        // 2. 필터링이 끝난 id를 기반으로 정보를 가져와 dto에 매핑
        List<SearchPlaceDto> places = dsl.select(
                        PLACE.PLACE_ID, // placeId
                        PLACE.PLACE_NAME.as("placeName"), // placeName
                        getAddressFieldForPlace(typeCode).as("address"), // address
                        PLC_CATEGORY.PLC_CATEGORY_NAME.as("placeType"), // placeType
                        PLACE.REVIEW_AVG, // reviewAverage
                        PLACE.REVIEW_COUNT, // reviewCount
                        PLACE.ENTER_PET_SIZE.as("weightLimit"), // weightLimit
                        PLACE.BUSINESS_HOUR, // businessHour
                        getLikeStatusFieldForPlace(memberId) // boolean likeStatus
                )
                .from(PLACE)
                .join(PLC_CATEGORY).on(PLACE.PLC_CATEGORY_ID.eq(PLC_CATEGORY.PLC_CATEGORY_ID))
                .where(PLACE.PLACE_ID.in(
                        dsl.select(filteredPlaceIdField).from(filteredPlaceSubquery)
                ))
                .orderBy(DSL.field("review_count").desc())
                .fetchInto(SearchPlaceDto.class);

        // filteredPlaceIds에 해당하는 태그 & 이미지 데이터를 한 번에 조회
        Map<Long, List<String>> tagsByPlaceId = dsl.select(PLACE_TAG.PLACE_ID, TAG.TAG_NAME)
                .from(PLACE_TAG)
                .join(TAG).on(PLACE_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(PLACE_TAG.PLACE_ID.in(
                        dsl.select(filteredPlaceIdField).from(filteredPlaceSubquery)
                ))
                .fetchGroups(PLACE_TAG.PLACE_ID, TAG.TAG_NAME);

        Map<Long, List<String>> imagesByPlaceId = dsl.select(PLACE_FILE.PLACE_ID, MEDIA_FILE.FILE_URL)
                .from(PLACE_FILE)
                .join(MEDIA_FILE).on(PLACE_FILE.MEDIA_FILE_ID.eq(MEDIA_FILE.MEDIA_FILE_ID))
                .where(PLACE_FILE.PLACE_ID.in(
                        dsl.select(filteredPlaceIdField).from(filteredPlaceSubquery)
                ))
                .fetchGroups(PLACE_FILE.PLACE_ID, MEDIA_FILE.FILE_URL);

        // 태그 & 이미지 데이터 매핑
        places.forEach(place -> {
            List<String> tags = tagsByPlaceId.getOrDefault(place.getPlaceId(), List.of());
            place.setTags(tags);

            List<String> images = imagesByPlaceId.getOrDefault(place.getPlaceId(), List.of());
            place.setImages(images);
        });

        boolean hasNext = places.size() > pageable.getPageSize();

        // slice는 다음에 데이터가 있는지 알 수 없기 때문에, 먼저 11개를 요청한 다음 11개가 들고와지면 10개로 줄여서 보내기
        if (hasNext) places.remove(places.size() - 1);

        return new SliceImpl<>(places, pageable, hasNext);
    }

    @Override
    public List<SearchPensionDto> searchPensions(List<Long> filteredPensionIds, String startDate, String endDate, String sizeCode, String typeCode,  Long memberId) {

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
                .fetchInto(SearchPensionDto.class); // DTO로 매핑

        // filteredPlaceIds에 해당하는 태그 & 이미지 데이터를 한 번에 조회
        Map<Long, List<String>> tagsByPensionId = dsl.select(PENSION_TAG.PENSION_ID, TAG.TAG_NAME)
                .from(PENSION_TAG)
                .join(TAG).on(PENSION_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(PENSION_TAG.PENSION_ID.in(filteredPensionIds))
                .fetchGroups(PENSION_TAG.PENSION_ID, TAG.TAG_NAME);

        Map<Long, List<String>> imagesByPensionId = dsl.select(PENSION_FILE.PENSION_ID, MEDIA_FILE.FILE_URL)
                .from(PENSION_FILE)
                .join(MEDIA_FILE).on(PENSION_FILE.MEDIA_FILE_ID.eq(MEDIA_FILE.MEDIA_FILE_ID))
                .where(PENSION_FILE.PENSION_ID.in(filteredPensionIds))
                .fetchGroups(PENSION_FILE.PENSION_ID, MEDIA_FILE.FILE_URL);

        // 태그 & 이미지 데이터 매핑
        pensions.forEach(pension -> {
            List<String> tags = tagsByPensionId.getOrDefault(pension.getPensionId(), List.of());
            pension.setTags(tags);

            List<String> images = imagesByPensionId.getOrDefault(pension.getPensionId(), List.of());
            pension.setImages(images);
        });

        return pensions;
    }

    @Override
    public Slice<Long> findPlaceIdsBySearchWordForMap(String searchWord, Pageable pageable) {
        List<Long> result = findPlaceIdsBySearchWordInternal(searchWord, pageable);

        boolean hasNext = result.size() > pageable.getPageSize();
        if (hasNext) result.remove(result.size() - 1);

        return new SliceImpl<>(result, pageable, hasNext);
    }

    @Override
    public List<Long> findPlaceIdsBySearchWord(String searchWord) {
        return findPlaceIdsBySearchWordInternal(searchWord, null);
    }

    private List<Long> findPlaceIdsBySearchWordInternal(String searchWord, Pageable pageable) {
        // 검색어를 단어별로 나누기
        String[] searchWords = searchWord.split(" ");

        // place 테이블에서 검색
        var placeQuery = dsl.select(PLACE.PLACE_ID)
                .from(PLACE)
                .where(
                        Arrays.stream(searchWords)
                                .map(word -> DSL.condition(
                                        "MATCH(place_name, plc_description) AGAINST (? IN NATURAL LANGUAGE MODE)", word + "*"
                                ))
                                .reduce(DSL.noCondition(), DSL::or)
                );

        // address 테이블에서 검색
        var addressQuery = dsl.select(PLC_PEN_ADDRESS.PLC_PEN_ID)
                .from(ADDRESS)
                .join(PLC_PEN_ADDRESS).on(PLC_PEN_ADDRESS.ADDRESS_ID.eq(ADDRESS.ADDRESS_ID))
                .where(
                        Arrays.stream(searchWords)
                                .map(word -> DSL.condition(
                                        "MATCH(address, province, city_district, subdistrict) AGAINST (? IN NATURAL LANGUAGE MODE)", word + "*"
                                ))
                                .reduce(DSL.noCondition(), DSL::or)
                );

        // UNION으로 검색 결과 합치기
        var combinedQuery = dsl.selectDistinct(DSL.field("place_id", Long.class))
                .from(placeQuery.union(addressQuery).asTable("combined_results"));

        // 페이징을 해야 되는 상황에서만 처리하도록
        if (pageable != null) {
            return combinedQuery.limit(pageable.getPageSize()+1)
                    .offset((int) pageable.getOffset())
                    .fetchInto(Long.class);
        }

        return combinedQuery.fetchInto(Long.class);
    }

    @Override
    public Slice<Long> findPensionIdsBySearchWord(String searchWord, Pageable pageable) {
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

        // 두 결과를 UNION으로 합치기
        List<Long> result =  dsl.selectDistinct(DSL.field("pension_id", Long.class))
                .from(pensionQuery.union(addressQuery).asTable("combined_results"))
                .limit(pageable.getPageSize())
                .offset((int) pageable.getOffset())
                .fetchInto(Long.class);

        boolean hasNext = result.size() > pageable.getPageSize();
        if (hasNext) result.remove(result.size() - 1);

        return new SliceImpl<>(result, pageable, hasNext);
    }

    private Field<String> getAddressFieldForPlace(String typeCode) {
        return getAddressField(PLACE.PLACE_ID, typeCode);
    }

    private Field<String> getAddressFieldForPension(String typeCode) {
        return getAddressField(PENSION.PENSION_ID, typeCode);
    }

    private Field<String> getAddressField(Field<Long> idField, String typeCode) {
        return dsl.select(ADDRESS.ADDRESS_)
                .from(PLC_PEN_ADDRESS)
                .join(ADDRESS).on(PLC_PEN_ADDRESS.ADDRESS_ID.eq(ADDRESS.ADDRESS_ID))
                .where(PLC_PEN_ADDRESS.PLC_PEN_ID.eq(idField))
                .and(PLC_PEN_ADDRESS.TYPE.eq(typeCode))
                .asField();
    }


    private Field<Boolean> getLikeStatusFieldForPlace(Long memberId) {
        return getLikeStatusField(memberId, LIKES.PLACE_ID, PLACE.PLACE_ID);
    }

    private Field<Boolean> getLikeStatusFieldForPension(Long memberId) {
        return getLikeStatusField(memberId, LIKES.PENSION_ID, PENSION.PENSION_ID);
    }

    private Field<Boolean> getLikeStatusField(Long memberId, Field<Long> idField1, Field<Long> idField2) {
        if (memberId == null) {
            return DSL.inline(false).as("likeStatus");
        }

        return DSL.when(
                        DSL.exists(
                                DSL.selectOne()
                                        .from(LIKES)
                                        .where(LIKES.MEMBER_ID.eq(memberId))
                                        .and(idField1.eq(idField2))
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

