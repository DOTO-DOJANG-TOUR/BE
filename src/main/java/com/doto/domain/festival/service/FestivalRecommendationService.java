package com.doto.domain.festival.service;

import com.doto.domain.festival.dto.FestivalCursor;
import com.doto.domain.festival.dto.FestivalShortResponseDTO;
import com.doto.domain.festival.dto.FestivalTodayResponseDTO;
import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.entity.code.GunguCodes;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.global.util.DateTimeUtils;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalRecommendationService {

    private final FestivalRepository festivalRepository;
    private final Clock applicationClock;

    public FestivalTodayResponseDTO getTodayFestivals(String cursor, int size) {
        FestivalCursor decoded = FestivalCursor.decode(cursor);
        Instant now = applicationClock.instant();

        // 다음 페이지 존재 여부 확인을 위해 요청 크기보다 하나 더 조회
        List<Festival> festivals = festivalRepository.findTodayFestivals(
                now,
                decoded == null ? null : decoded.eventEndDate(),
                decoded == null ? null : decoded.eventStartDate(),
                decoded == null ? null : decoded.title(),
                PageRequest.ofSize(size + 1)
        );

        boolean hasNext = festivals.size() > size;
        List<Festival> page = hasNext ? festivals.subList(0, size) : festivals;
        String nextCursor = hasNext ? toCursor(page.get(page.size() - 1)) : null;

        List<FestivalShortResponseDTO> responses = page.stream()
                .map(this::toShortResponse)
                .toList();
        return new FestivalTodayResponseDTO(responses, nextCursor);
    }

    private String toCursor(Festival festival) {
        return new FestivalCursor(festival.getEventEndDate(), festival.getEventStartDate(), festival.getTitle())
                .encode();
    }

    private FestivalShortResponseDTO toShortResponse(Festival festival) {
        ZoneId zone = applicationClock.getZone();
        return new FestivalShortResponseDTO(
                festival.getTitle(),
                festival.getImageUrl(),
                DateTimeUtils.toDateString(festival.getEventStartDate(), zone),
                DateTimeUtils.toDateString(festival.getEventEndDate(), zone),
                GunguCodes.findName(festival.getLegalRegion(), festival.getLegalGungu())
        );
    }
}
