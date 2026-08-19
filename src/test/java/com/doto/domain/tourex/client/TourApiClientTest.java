package com.doto.domain.tourex.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.doto.domain.tourex.exception.TourApiException;
import java.net.URI;
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
