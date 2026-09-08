package com.doto.domain.tourex.client;

import com.doto.domain.tourex.dto.FestivalIntroApiResponseDTO;
import com.doto.domain.tourex.dto.TourApiResponseDTO;
import com.doto.domain.tourex.dto.TourImageApiResponseDTO;
import com.doto.domain.tourex.exception.TourApiErrorCode;
import com.doto.domain.tourex.exception.TourApiException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourApiClient {

    // TourAPI 초당 요청 제한(429) 대응을 위한 최소 호출 간격 및 재시도 설정
    private static final long MIN_REQUEST_INTERVAL_MILLIS = 120L;
    private static final int MAX_RATE_LIMIT_RETRY_ATTEMPTS = 3;
    private static final long RATE_LIMIT_RETRY_BACKOFF_MILLIS = 400L;

    private final RestClient tourApiRestClient;
    private final Object requestPacingLock = new Object();
    private long lastRequestAtMillis = 0L;

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

    // 관광지 상세 이미지 갤러리 조회, 원본 이미지가 없는 관광지도 있어 빈 목록을 그대로 반환
    public List<TourImageApiResponseDTO.TourImageDTO> getContentImages(Long contentId) {
        TourImageApiResponseDTO response = get(
                "/detailImage2?contentId={contentId}&imageYN=Y&numOfRows=100&pageNo=1"
                        + "&MobileOS=ETC&MobileApp=DOTO&_type=json&serviceKey={serviceKey}",
                Map.of("contentId", contentId),
                TourImageApiResponseDTO.class
        );
        return response.itemsOrEmpty();
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
        //행사정보조회 EV 만 뽑아옴
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

    // 429(요청 한도 초과)는 호출 간격을 두고 재시도, 그 외 오류는 즉시 전파
    private <T> T get(String uri, Map<String, ?> uriVariables, Class<T> responseType) {
        TourApiException rateLimitException = null;
        for (int attempt = 1; attempt <= MAX_RATE_LIMIT_RETRY_ATTEMPTS; attempt++) {
            awaitRequestInterval();
            try {
                T response = tourApiRestClient.get()
                        .uri(uri, uriVariables)
                        .retrieve()
                        .body(responseType);
                validateResponse(response);
                return response;
            } catch (ResourceAccessException exception) {
                throw new TourApiException(TourApiErrorCode.TOUR_API_UNAVAILABLE, exception);
            } catch (TourApiException exception) {
                if (exception.getErrorCode() != TourApiErrorCode.TOUR_API_RATE_LIMITED) {
                    throw exception;
                }
                rateLimitException = exception;
                log.warn("TourAPI 요청 한도 초과로 재시도합니다: attempt={}/{}", attempt, MAX_RATE_LIMIT_RETRY_ATTEMPTS);
                sleep(RATE_LIMIT_RETRY_BACKOFF_MILLIS * attempt);
            } catch (RestClientException exception) {
                throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR, exception);
            }
        }
        throw rateLimitException;
    }

    // 연속 호출 사이 최소 간격을 보장해 초당 요청 제한(429) 발생 자체를 줄인다
    private void awaitRequestInterval() {
        synchronized (requestPacingLock) {
            long waitMillis = MIN_REQUEST_INTERVAL_MILLIS - (System.currentTimeMillis() - lastRequestAtMillis);
            if (waitMillis > 0) {
                sleep(waitMillis);
            }
            lastRequestAtMillis = System.currentTimeMillis();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new TourApiException(TourApiErrorCode.TOUR_API_UNAVAILABLE, exception);
        }
    }

    private void validateResponse(Object response) {
        String resultCode = switch (response) {
            case TourApiResponseDTO tourApiResponse -> getResultCode(tourApiResponse.response());
            case FestivalIntroApiResponseDTO festivalIntroResponse -> getResultCode(festivalIntroResponse.response());
            case TourImageApiResponseDTO tourImageResponse -> getResultCode(tourImageResponse.response());
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

    private String getResultCode(TourImageApiResponseDTO.Response response) {
        return response == null || response.header() == null ? null : response.header().resultCode();
    }
}
