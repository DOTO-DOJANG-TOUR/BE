package com.doto.domain.festival.service;

import com.doto.domain.festival.dto.FestivalEndDateCursor;
import com.doto.domain.festival.dto.FestivalPageResponseDTO;
import com.doto.domain.festival.dto.FestivalRegionPageResponseDTO;
import com.doto.domain.festival.dto.FestivalRegionResponseDTO;
import com.doto.domain.festival.dto.FestivalShortResponseDTO;
import com.doto.domain.festival.dto.FestivalStatus;
import com.doto.domain.festival.dto.FestivalUpcomingCursor;
import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.entity.code.GunguCodes;
import com.doto.domain.festival.entity.enums.RegionGroup;
import com.doto.domain.festival.repository.FestivalRepository;
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

    private static final String FIRST_PAGE_TITLE = "";
    // duration 첫 페이지 sentinel, 실제 축제 기간은 항상 0 이상이라 -1초면 항상 첫 분기로 통과
    private static final Duration FIRST_PAGE_DURATION = Duration.ofSeconds(-1);

    private final FestivalRepository festivalRepository;
    private final Clock applicationClock;

    public FestivalPageResponseDTO getTodayFestivals(String cursor, int size) {
        FestivalEndDateCursor decoded = FestivalEndDateCursor.decode(cursor);
        Instant now = applicationClock.instant();
        List<Festival> festivals = festivalRepository.findTodayFestivals(
                now,
                decoded != null ? decoded.eventEndDate() : Instant.EPOCH,
                decoded != null ? decoded.title() : FIRST_PAGE_TITLE,
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
                decoded != null ? decoded.title() : FIRST_PAGE_TITLE,
                PageRequest.ofSize(size + 1)
        );
        boolean hasNext = festivals.size() > size;
        List<Festival> page = hasNext ? festivals.subList(0, size) : festivals;
        String nextCursor = hasNext ? toUpcomingCursor(page.get(page.size() - 1)) : null;
        List<FestivalShortResponseDTO> responses = page.stream().map(this::toShortResponse).toList();
        return new FestivalPageResponseDTO(responses, nextCursor);
    }

    public FestivalRegionPageResponseDTO getFestivalsByRegion(RegionGroup regionGroup, String cursor, int size) {
        FestivalEndDateCursor decoded = FestivalEndDateCursor.decode(cursor);
        Instant now = applicationClock.instant();
        List<Festival> festivals = festivalRepository.findByRegionGroup(
                regionGroup.getRegions(),
                now,
                decoded != null ? decoded.eventEndDate() : Instant.EPOCH,
                decoded != null ? decoded.title() : FIRST_PAGE_TITLE,
                PageRequest.ofSize(size + 1)
        );
        boolean hasNext = festivals.size() > size;
        List<Festival> page = hasNext ? festivals.subList(0, size) : festivals;
        String nextCursor = hasNext ? toEndDateCursor(page.get(page.size() - 1)) : null;
        List<FestivalRegionResponseDTO> responses = page.stream()
                .map(festival -> toRegionResponse(festival, now))
                .toList();
        return new FestivalRegionPageResponseDTO(responses, nextCursor);
    }

    private String toEndDateCursor(Festival festival) {
        return new FestivalEndDateCursor(festival.getEventEndDate(), festival.getTitle()).encode();
    }

    private String toUpcomingCursor(Festival festival) {
        Duration duration = Duration.between(festival.getEventStartDate(), festival.getEventEndDate());
        return new FestivalUpcomingCursor(festival.getEventStartDate(), duration, festival.getTitle()).encode();
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

    // 조회 조건상 eventEndDate>=now라 eventStartDate<=now면 개최중, 아니면 개최전
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
                festival.getCategory()
        );
    }

}
