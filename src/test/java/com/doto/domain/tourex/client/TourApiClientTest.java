package com.doto.domain.tourex.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.doto.domain.tourex.exception.TourApiException;
import java.net.URI;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DisplayName("TourApiClient 테스트")
class TourApiClientTest {

    private TourApiClient tourApiClient;

    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder
                .baseUrl("https://tour-api.test")
                .defaultUriVariables(Map.of("serviceKey", "test-service-key"))
                .build();
        tourApiClient = new TourApiClient(restClient);
    }

    @Test
    @DisplayName("관광지 상세 요청에 공통 파라미터와 서비스 키를 포함한다")
    void requestsTourSpotDetailWithCommonParameters() {
        server.expect(once(), this::assertContentDetailRequest)
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(successResponse(), MediaType.APPLICATION_JSON));

        tourApiClient.getContentDetail(126516L);

        server.verify();
    }

    @Test
    @DisplayName("HTTP 200이어도 TourAPI resultCode가 실패면 예외를 던진다")
    void throwsExceptionWhenTourApiResultCodeIsFailure() {
        server.expect(this::assertContentDetailRequest)
                .andRespond(withSuccess("""
                        {"response":{"header":{"resultCode":"0301","resultMsg":"SERVICE ERROR"},"body":{"items":{"item":[]}}}}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> tourApiClient.getContentDetail(126516L))
                .isInstanceOf(TourApiException.class);
    }

    @Test
    @DisplayName("주변 관광지가 없을 때 items 빈 문자열을 빈 목록으로 처리한다")
    void returnsEmptyListWhenNearbyTourApiItemsIsBlankString() {
        server.expect(request -> assertThat(request.getURI().getPath()).isEqualTo("/locationBasedList2"))
                .andRespond(withSuccess("""
                        {"response":{"header":{"resultCode":"0000","resultMsg":"OK"},"body":{"items":""}}}
                        """, MediaType.APPLICATION_JSON));

        assertThat(tourApiClient.getNearbyTourSpots(
                new java.math.BigDecimal("126.97"), new java.math.BigDecimal("37.56"), 5_000)).isEmpty();
    }

    @Test
    @DisplayName("축제 범위 조회 결과가 100건을 넘으면 다음 페이지도 조회한다")
    void retrievesAllFestivalPages() {
        server.expect(once(), request -> assertThat(request.getURI().getRawQuery()).contains("pageNo=1"))
                .andRespond(withSuccess(festivalResponse(101, 1L), MediaType.APPLICATION_JSON));
        server.expect(once(), request -> assertThat(request.getURI().getRawQuery()).contains("pageNo=2"))
                .andRespond(withSuccess(festivalResponse(101, 2L), MediaType.APPLICATION_JSON));

        var response = tourApiClient.searchFestivals(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 10, 2), null, null);

        assertThat(response.itemsOrEmpty()).extracting(item -> item.contentId()).containsExactly(1L, 2L);
        server.verify();
    }

    private String festivalResponse(int totalCount, long contentId) {
        return """
                {"response":{"header":{"resultCode":"0000","resultMsg":"OK"},"body":{
                "items":{"item":[{"contentid":%d}]},"numOfRows":100,"pageNo":1,"totalCount":%d}}}
                """.formatted(contentId, totalCount);
    }

    private String successResponse() {
        return """
                {"response":{"header":{"resultCode":"0000","resultMsg":"OK"},"body":{"items":{"item":[]}}}}
                """;
    }

    private void assertContentDetailRequest(org.springframework.http.client.ClientHttpRequest request) {
        URI uri = request.getURI();
        assertThat(uri.getPath()).isEqualTo("/detailCommon2");
        assertThat(uri.getRawQuery()).contains(
                "contentId=126516",
                "MobileOS=ETC",
                "MobileApp=DOTO",
                "_type=json",
                "serviceKey=test-service-key"
        );
    }
}
