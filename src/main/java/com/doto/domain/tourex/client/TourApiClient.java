package com.doto.domain.tourex.client;

import com.doto.domain.tourex.dto.FestivalIntroApiResponseDTO;
import com.doto.domain.tourex.dto.TourApiResponseDTO;
import com.doto.domain.tourex.exception.TourApiErrorCode;
import com.doto.domain.tourex.exception.TourApiException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class TourApiClient {

    private final RestClient tourApiRestClient;

    public TourApiResponseDTO getContentDetail(Long contentId) {
        return get(
                "/detailCommon2?contentId={contentId}&MobileOS=ETC&MobileApp=DOTO&_type=json&serviceKey={serviceKey}",
                Map.of("contentId", contentId),
                TourApiResponseDTO.class
        );
    }

    public FestivalIntroApiResponseDTO getFestivalIntro(Long contentId) {
        return get(
                "/detailIntro2?contentId={contentId}&contentTypeId=15"
                        + "&MobileOS=ETC&MobileApp=DOTO&_type=json&serviceKey={serviceKey}",
                Map.of("contentId", contentId),
                FestivalIntroApiResponseDTO.class
        );
    }

    public List<TourApiResponseDTO.TourContentDTO> getNearbyTourSpots(
            BigDecimal longitude,
            BigDecimal latitude,
            int radius
    ) {
        if (radius <= 0 || radius > 20_000) {
            throw new IllegalArgumentException("관광지 조회 반경은 1m 이상 20km 이하여야 합니다.");
        }

        TourApiResponseDTO response = get(
                "/locationBasedList2?mapX={longitude}&mapY={latitude}&radius={radius}"
                        + "&contentTypeId=12&arrange=E&numOfRows=100&pageNo=1"
                        + "&MobileOS=ETC&MobileApp=DOTO&_type=json&serviceKey={serviceKey}",
                Map.of("longitude", longitude, "latitude", latitude, "radius", radius),
                TourApiResponseDTO.class
        );
        return response.itemsOrEmpty();
    }

    // 축제 데이터 초기 적재·동기화에만 사용한다
    public TourApiResponseDTO searchFestivals(
            LocalDate eventStartDate,
            LocalDate eventEndDate,
            String legalDongRegionCode,
            String legalDongSigunguCode
    ) {
        if (legalDongSigunguCode != null && legalDongRegionCode == null) {
            throw new IllegalArgumentException("법정동 시군구 코드는 시도 코드와 함께 입력해야 합니다.");
        }

        StringBuilder uri = new StringBuilder(
                "/searchFestival2?eventStartDate={eventStartDate}&numOfRows=100&pageNo={pageNo}"
                        + "&MobileOS=ETC&MobileApp=DOTO&_type=json&serviceKey={serviceKey}"
        );
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("eventStartDate", eventStartDate.format(DateTimeFormatter.BASIC_ISO_DATE));
        uriVariables.put("pageNo", 1);

        appendDateQueryParam(uri, uriVariables, "eventEndDate", eventEndDate);
        appendQueryParam(uri, uriVariables, "lDongRegnCd", legalDongRegionCode);
        appendQueryParam(uri, uriVariables, "lDongSignguCd", legalDongSigunguCode);

        TourApiResponseDTO firstPage = get(uri.toString(), uriVariables, TourApiResponseDTO.class);
        int totalCount = firstPage.response().body().totalCount() == null ? 0 : firstPage.response().body().totalCount();
        if (totalCount <= 100) {
            return firstPage;
        }

        List<TourApiResponseDTO.TourContentDTO> festivals = new java.util.ArrayList<>(firstPage.itemsOrEmpty());
        int pageCount = (totalCount + 99) / 100;
        for (int pageNo = 2; pageNo <= pageCount; pageNo++) {
            uriVariables.put("pageNo", pageNo);
            festivals.addAll(get(uri.toString(), uriVariables, TourApiResponseDTO.class).itemsOrEmpty());
        }

        return new TourApiResponseDTO(new TourApiResponseDTO.Response(
                firstPage.response().header(),
                new TourApiResponseDTO.Body(
                        new TourApiResponseDTO.Items(festivals), 100, 1, totalCount)
        ));
    }

    private void appendDateQueryParam(
            StringBuilder uri,
            Map<String, Object> uriVariables,
            String name,
            LocalDate value
    ) {
        if (value != null) {
            appendQueryParam(uri, uriVariables, name, value.format(DateTimeFormatter.BASIC_ISO_DATE));
        }
    }

    private void appendQueryParam(StringBuilder uri, Map<String, Object> uriVariables, String name, String value) {
        if (value != null) {
            uri.append("&").append(name).append("={").append(name).append("}");
            uriVariables.put(name, value);
        }
    }

    private <T> T get(String uri, Map<String, ?> uriVariables, Class<T> responseType) {
        try {
            T response = tourApiRestClient.get()
                    .uri(uri, uriVariables)
                    .retrieve()
                    .body(responseType);
            validateResponse(response);
            return response;
        } catch (ResourceAccessException exception) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_UNAVAILABLE, exception);
        } catch (RestClientException exception) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR, exception);
        }
    }

    private void validateResponse(Object response) {
        String resultCode = switch (response) {
            case TourApiResponseDTO tourApiResponse -> getResultCode(tourApiResponse.response());
            case FestivalIntroApiResponseDTO festivalIntroResponse -> getResultCode(festivalIntroResponse.response());
            case null, default -> null;
        };

        if (!"0000".equals(resultCode)) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR);
        }
    }

    private String getResultCode(TourApiResponseDTO.Response response) {
        return response == null || response.header() == null ? null : response.header().resultCode();
    }

    private String getResultCode(FestivalIntroApiResponseDTO.Response response) {
        return response == null || response.header() == null ? null : response.header().resultCode();
    }
}
