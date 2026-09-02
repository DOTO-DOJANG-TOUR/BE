package com.doto.global.config;

import com.doto.domain.tourex.exception.TourApiErrorCode;
import com.doto.domain.tourex.exception.TourApiException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

@Configuration
@Slf4j
public class TourApiClientConfig {

    @Bean
    @ConditionalOnMissingBean(RestClient.Builder.class)
    public RestClient.Builder tourApiRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient tourApiRestClient(
            RestClient.Builder builder,
            @Value("${tour-api.base-url}") String baseUrl,
            @Value("${tour-api.service-key}") String serviceKey,
            @Value("${tour-api.connect-timeout}") java.time.Duration connectTimeout,
            @Value("${tour-api.read-timeout}") java.time.Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(baseUrl)
                .defaultUriVariables(Map.of("serviceKey", serviceKey))
                .requestFactory(requestFactory)
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
                            log.warn("TourAPI HTTP 오류: status={}, uri={}, body={}",
                                    response.getStatusCode(), maskServiceKey(request.getURI().toString()), responseBody);
                            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR);
                        }
                )
                .build();
    }

    private String maskServiceKey(String uri) {
        return uri.replaceAll("([?&]serviceKey=)[^&]*", "$1REDACTED");
    }
}
