package com.doto.domain.festival.service;

import com.doto.domain.festival.dto.FestivalDetailResponseDTO;
import com.doto.domain.festival.dto.FestivalEndDateCursor;
import com.doto.domain.festival.dto.FestivalPageResponseDTO;
import com.doto.domain.festival.dto.FestivalRegionPageResponseDTO;
import com.doto.domain.festival.dto.FestivalRegionResponseDTO;
import com.doto.domain.festival.dto.FestivalSearchCursor;
import com.doto.domain.festival.dto.FestivalShortResponseDTO;
import com.doto.domain.festival.dto.FestivalStatus;
import com.doto.domain.festival.dto.FestivalUpcomingCursor;
import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.entity.code.GunguCodes;
import com.doto.domain.festival.entity.enums.FestivalSort;
import com.doto.domain.festival.entity.enums.Region;
import com.doto.domain.festival.entity.enums.RegionGroup;
import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.festival.exception.FestivalException;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.festival.repository.FestivalSearchRow;
import com.doto.global.util.DateTimeUtils;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalRecommendationService {

    private static final Long FIRST_PAGE_ID = 0L;
    // duration 첫 페이지 sentinel, 실제 축제 기간은 항상 0 이상이라 -1초면 항상 첫 분기로 통과
    private static final Duration FIRST_PAGE_DURATION = Duration.ofSeconds(-1);
    // 유사도 첫 페이지 sentinel, 실제 유사도는 0~1 사이라 2.0이면 항상 첫 분기로 통과
    private static final double FIRST_PAGE_SIMILARITY = 2.0;
    // parking 값에 이 문구가 포함되면 parkingFee도 불가능으로 덮어씀
    private static final String PARKING_UNAVAILABLE_KEYWORD = "불가능";
    // 검색어에 포함되면 상태 필터로 취급하는 키워드
    private static final Set<String> ONGOING_KEYWORDS = Set.of("진행", "오늘");
    private static final Set<String> UPCOMING_KEYWORDS = Set.of("예정", "내일");

    private final FestivalRepository festivalRepository;
    private final Clock applicationClock;

    public FestivalPageResponseDTO getTodayFestivals(String cursor, int size) {
        FestivalEndDateCursor decoded = FestivalEndDateCursor.decode(cursor);
        Instant now = applicationClock.instant();
        List<Festival> festivals = festivalRepository.findTodayFestivals(
                now,
                decoded != null ? decoded.eventEndDate() : Instant.EPOCH,
                decoded != null ? decoded.id() : FIRST_PAGE_ID,
                PageRequest.ofSize(size + 1)
        );
        boolean hasNext = festivals.size() > size;
        List<Festival> page = hasNext ? festivals.subList(0, size) : festivals;
        String nextCursor = hasNext ? toEndDateCursor(page.get(page.size() - 1)) : null;
        List<FestivalShortResponseDTO> responses = page.stream().map(this::toShortResponse).toList();
        return new FestivalPageResponseDTO(responses, nextCursor);
    }

    public FestivalPageResponseDTO getUpcomingFestivals(String cursor, int size) {
        FestivalUpcomingCursor decoded = FestivalUpcomingCursor.decode(cursor);
        Instant now = applicationClock.instant();
        List<Festival> festivals = festivalRepository.findUpcomingFestivals(
                now,
                decoded != null ? decoded.eventStartDate() : Instant.EPOCH,
                decoded != null ? decoded.duration() : FIRST_PAGE_DURATION,
                decoded != null ? decoded.id() : FIRST_PAGE_ID,
                PageRequest.ofSize(size + 1)
        );
        boolean hasNext = festivals.size() > size;
        List<Festival> page = hasNext ? festivals.subList(0, size) : festivals;
        String nextCursor = hasNext ? toUpcomingCursor(page.get(page.size() - 1)) : null;
        List<FestivalShortResponseDTO> responses = page.stream().map(this::toShortResponse).toList();
        return new FestivalPageResponseDTO(responses, nextCursor);
    }

    // 종료임박순은 오늘의 축제와 같은 필터(진행중), 개최임박순은 앞으로의 축제와 같은 필터(개최 전) + 지역 조건만 추가
    public FestivalRegionPageResponseDTO getFestivalsByRegion(
            RegionGroup regionGroup, FestivalSort sort, String cursor, int size
    ) {
        Instant now = applicationClock.instant();
        List<Festival> festivals = sort == FestivalSort.START_DATE
                ? findRegionFestivalsByStartDate(regionGroup, cursor, size, now)
                : findRegionFestivalsByEndDate(regionGroup, cursor, size, now);

        boolean hasNext = festivals.size() > size;
        List<Festival> page = hasNext ? festivals.subList(0, size) : festivals;
        String nextCursor = hasNext
                ? (sort == FestivalSort.START_DATE ? toUpcomingCursor(page.get(page.size() - 1)) : toEndDateCursor(page.get(page.size() - 1)))
                : null;
        List<FestivalRegionResponseDTO> responses = page.stream()
                .map(festival -> toRegionResponse(festival, now))
                .toList();
        return new FestivalRegionPageResponseDTO(responses, nextCursor);
    }

    private List<Festival> findRegionFestivalsByEndDate(RegionGroup regionGroup, String cursor, int size, Instant now) {
        FestivalEndDateCursor decoded = FestivalEndDateCursor.decode(cursor);
        return festivalRepository.findByRegionGroupOrderByEndDate(
                regionGroup.getRegions(),
                now,
                decoded != null ? decoded.eventEndDate() : Instant.EPOCH,
                decoded != null ? decoded.id() : FIRST_PAGE_ID,
                PageRequest.ofSize(size + 1)
        );
    }

    private List<Festival> findRegionFestivalsByStartDate(RegionGroup regionGroup, String cursor, int size, Instant now) {
        FestivalUpcomingCursor decoded = FestivalUpcomingCursor.decode(cursor);
        return festivalRepository.findByRegionGroupOrderByStartDate(
                regionGroup.getRegions(),
                now,
                decoded != null ? decoded.eventStartDate() : Instant.EPOCH,
                decoded != null ? decoded.duration() : FIRST_PAGE_DURATION,
                decoded != null ? decoded.id() : FIRST_PAGE_ID,
                PageRequest.ofSize(size + 1)
        );
    }

    // 통합 검색: query에서 상태 키워드(진행/오늘, 예정/내일)를 뽑아 status 필터로, 나머지 텍스트를 키워드로 사용
    // title 제목 유사도(pg_trgm) 높은 순으로 정렬(주소/지역/카테고리는 인덱스가 없어 매칭 대상에서 제외)
    public FestivalRegionPageResponseDTO searchFestivals(String query, String cursor, int size) {
        ParsedSearchQuery parsed = parseSearchQuery(query);
        FestivalSearchCursor decoded = FestivalSearchCursor.decode(cursor);
        Instant now = applicationClock.instant();

        List<FestivalSearchRow> rows = festivalRepository.searchFestivals(
                parsed.keyword(),
                parsed.includeOngoing(),
                parsed.includeUpcoming(),
                now,
                decoded != null ? decoded.similarity() : FIRST_PAGE_SIMILARITY,
                decoded != null ? decoded.eventEndDate() : Instant.EPOCH,
                decoded != null ? decoded.id() : FIRST_PAGE_ID,
                size + 1
        );

        boolean hasNext = rows.size() > size;
        List<FestivalSearchRow> page = hasNext ? rows.subList(0, size) : rows;
        String nextCursor = hasNext ? toSearchCursor(page.get(page.size() - 1)) : null;
        List<FestivalRegionResponseDTO> responses = page.stream()
                .map(row -> toSearchResponse(row, now))
                .toList();
        return new FestivalRegionPageResponseDTO(responses, nextCursor);
    }

    // query에서 상태 키워드를 제거하고 남은 텍스트를 검색 키워드로 사용, 상태 키워드가 하나도 없으면 진행중+개최전 전체를 대상으로 함
    private ParsedSearchQuery parseSearchQuery(String query) {
        String remaining = query == null ? "" : query;
        boolean includeOngoing = false;
        boolean includeUpcoming = false;
        for (String keyword : ONGOING_KEYWORDS) {
            if (remaining.contains(keyword)) {
                includeOngoing = true;
                remaining = remaining.replace(keyword, "");
            }
        }
        for (String keyword : UPCOMING_KEYWORDS) {
            if (remaining.contains(keyword)) {
                includeUpcoming = true;
                remaining = remaining.replace(keyword, "");
            }
        }
        if (!includeOngoing && !includeUpcoming) {
            includeOngoing = true;
            includeUpcoming = true;
        }
        String keyword = remaining.isBlank() ? null : remaining.trim();
        return new ParsedSearchQuery(keyword, includeOngoing, includeUpcoming);
    }

    private record ParsedSearchQuery(String keyword, boolean includeOngoing, boolean includeUpcoming) {
    }

    public FestivalDetailResponseDTO getFestivalDetail(Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new FestivalException(FestivalErrorCode.FESTIVAL_NOT_FOUND));
        Instant now = applicationClock.instant();
        return new FestivalDetailResponseDTO(
                festival.getImageUrl(),
                festival.getTitle(),
                toDetailStatus(festival, now),
                festival.getCategory(),
                festival.getAddress(),
                festival.getPhone(),
                festival.getHomepageUrl(),
                festival.getSummary(),
                festival.getProgram(),
                festival.getOperationHours(),
                festival.getRestDate(),
                festival.getUseFee(),
                resolveParkingFee(festival)
        );
    }

    private String toEndDateCursor(Festival festival) {
        return new FestivalEndDateCursor(festival.getEventEndDate(), festival.getId()).encode();
    }

    private String toUpcomingCursor(Festival festival) {
        Duration duration = Duration.between(festival.getEventStartDate(), festival.getEventEndDate());
        return new FestivalUpcomingCursor(festival.getEventStartDate(), duration, festival.getId()).encode();
    }

    private FestivalShortResponseDTO toShortResponse(Festival festival) {
        ZoneId zone = applicationClock.getZone();
        return new FestivalShortResponseDTO(
                String.valueOf(festival.getId()),
                festival.getTitle(),
                festival.getImageUrl(),
                DateTimeUtils.toDateString(festival.getEventStartDate(), zone),
                DateTimeUtils.toDateString(festival.getEventEndDate(), zone),
                GunguCodes.findName(festival.getLegalRegion(), festival.getLegalGungu())
        );
    }

    // 조회 조건상 종료임박순=진행중만, 개최임박순=개최전만 나오므로 eventStartDate 기준으로만 판단해도 충분
    private FestivalRegionResponseDTO toRegionResponse(Festival festival, Instant now) {
        ZoneId zone = applicationClock.getZone();
        FestivalStatus status = !festival.getEventStartDate().isAfter(now) ? FestivalStatus.ONGOING : FestivalStatus.UPCOMING;
        return new FestivalRegionResponseDTO(
                String.valueOf(festival.getId()),
                festival.getImageUrl(),
                status,
                festival.getTitle(),
                GunguCodes.findName(festival.getLegalRegion(), festival.getLegalGungu()),
                DateTimeUtils.toDateString(festival.getEventStartDate(), zone),
                DateTimeUtils.toDateString(festival.getEventEndDate(), zone),
                festival.getCategory()
        );
    }

    private String toSearchCursor(FestivalSearchRow row) {
        return new FestivalSearchCursor(row.getSimilarityScore(), row.getEventEndDate(), row.getFestivalId()).encode();
    }

    // 검색 결과도 필터상 진행중/개최전만 나오므로 지역별 조회와 동일하게 eventStartDate 기준으로만 판단
    private FestivalRegionResponseDTO toSearchResponse(FestivalSearchRow row, Instant now) {
        ZoneId zone = applicationClock.getZone();
        FestivalStatus status = !row.getEventStartDate().isAfter(now) ? FestivalStatus.ONGOING : FestivalStatus.UPCOMING;
        Region region = row.getRegion() != null ? Region.valueOf(row.getRegion()) : null;
        return new FestivalRegionResponseDTO(
                String.valueOf(row.getFestivalId()),
                row.getImageUrl(),
                status,
                row.getTitle(),
                GunguCodes.findName(region, row.getGungu()),
                DateTimeUtils.toDateString(row.getEventStartDate(), zone),
                DateTimeUtils.toDateString(row.getEventEndDate(), zone),
                row.getCategory()
        );
    }

    // 상세보기는 목록과 달리 이미 종료된 축제도 조회될 수 있어 종료 상태까지 판단
    private FestivalStatus toDetailStatus(Festival festival, Instant now) {
        if (festival.getEventEndDate().isBefore(now)) {
            return FestivalStatus.ENDED;
        }
        return !festival.getEventStartDate().isAfter(now) ? FestivalStatus.ONGOING : FestivalStatus.UPCOMING;
    }

    // parking에 "불가능"이 포함되면 요금도 불가능으로 표시
    private String resolveParkingFee(Festival festival) {
        String parking = festival.getParking();
        if (parking != null && parking.contains(PARKING_UNAVAILABLE_KEYWORD)) {
            return PARKING_UNAVAILABLE_KEYWORD;
        }
        return festival.getParkingFee();
    }

}
