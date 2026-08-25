package com.doto.domain.festival.service;

import com.doto.domain.festival.dto.FestivalDetailResponseDTO;
import com.doto.domain.festival.dto.FestivalEndDateCursor;
import com.doto.domain.festival.dto.FestivalPageResponseDTO;
import com.doto.domain.festival.dto.FestivalRegionPageResponseDTO;
import com.doto.domain.festival.dto.FestivalRegionResponseDTO;
import com.doto.domain.festival.dto.FestivalShortResponseDTO;
import com.doto.domain.festival.dto.FestivalStatus;
import com.doto.domain.festival.dto.FestivalUpcomingCursor;
import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.entity.code.GunguCodes;
import com.doto.domain.festival.entity.enums.FestivalSort;
import com.doto.domain.festival.entity.enums.RegionGroup;
import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.festival.exception.FestivalException;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.tourex.enums.TourApiCategory;
import com.doto.global.util.DateTimeUtils;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalRecommendationService {

    private static final Long FIRST_PAGE_ID = 0L;
    // duration 첫 페이지 sentinel, 실제 축제 기간은 항상 0 이상이라 -1초면 항상 첫 분기로 통과
    private static final Duration FIRST_PAGE_DURATION = Duration.ofSeconds(-1);
    // parking 값에 이 문구가 포함되면 parkingFee도 불가능으로 덮어씀
    private static final String PARKING_UNAVAILABLE_KEYWORD = "불가능";

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

    public FestivalDetailResponseDTO getFestivalDetail(Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new FestivalException(FestivalErrorCode.FESTIVAL_NOT_FOUND));
        Instant now = applicationClock.instant();
        return new FestivalDetailResponseDTO(
                festival.getImageUrl(),
                festival.getTitle(),
                toDetailStatus(festival, now),
                toCategoryLabel(festival.getCategory()),
                festival.getAddress(),
                festival.getPhone(),
                festival.getHomepageUrl(),
                festival.getSummary(),
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
                festival.getId(),
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
                festival.getId(),
                festival.getImageUrl(),
                status,
                festival.getTitle(),
                GunguCodes.findName(festival.getLegalRegion(), festival.getLegalGungu()),
                DateTimeUtils.toDateString(festival.getEventStartDate(), zone),
                DateTimeUtils.toDateString(festival.getEventEndDate(), zone),
                toCategoryLabel(festival.getCategory())
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

    private String toCategoryLabel(String lclsSystem1) {
        TourApiCategory category = TourApiCategory.fromLclsSystem1Code(lclsSystem1);
        return category != null ? category.name() : null;
    }

}
