package com.doto.domain.tourex.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.doto.domain.stamp.entity.enums.TourSpotCategory;
import com.doto.domain.tourex.client.TourApiClient;
import com.doto.domain.tourex.dto.TourApiResponseDTO;
import com.doto.domain.tourex.exception.TourApiException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TourApiService 테스트")
class TourApiServiceTest {

    @Mock
    private TourApiClient tourApiClient;

    @InjectMocks
    private TourApiService tourApiService;

    @Nested
    @DisplayName("관광지 상세 조회")
    class GetTourInfo {

        @Test
        @DisplayName("TourAPI 콘텐츠를 관광지 상세 응답으로 변환한다")
        void mapsTourSpotDetail() {
            given(tourApiClient.getContentDetail(126516L)).willReturn(response(content("VE", null, "fallback.jpg")));

            var result = tourApiService.getTourInfo(126516L);

            assertThat(result.title()).isEqualTo("보신각터");
            assertThat(result.imageUrl()).isEqualTo("fallback.jpg");
            assertThat(result.tourSpotCategory()).isEqualTo(TourSpotCategory.문화관광);
        }

        @Test
        @DisplayName("콘텐츠가 없으면 TourAPI 응답 예외를 던진다")
        void throwsExceptionWhenContentIsEmpty() {
            given(tourApiClient.getContentDetail(126516L)).willReturn(response());

            assertThatThrownBy(() -> tourApiService.getTourInfo(126516L))
                    .isInstanceOf(TourApiException.class);
        }
    }

    @Nested
    @DisplayName("주변 관광지 조회")
    class GetNearbyTourSpots {

        @Test
        @DisplayName("좌표 기반 관광지 목록을 응답 DTO로 변환한다")
        void mapsNearbyTourSpots() {
            given(tourApiClient.getNearbyTourSpots(new BigDecimal("126.97"), new BigDecimal("37.56"), 5_000))
                    .willReturn(List.of(content("HS", "image.jpg", null)));

            var result = tourApiService.getNearbyTourSpots("126.97", "37.56");

            assertThat(result).singleElement().satisfies(tourSpot -> {
                assertThat(tourSpot.tourSpotCategory()).isEqualTo(TourSpotCategory.역사관광);
                assertThat(tourSpot.imageUrl()).isEqualTo("image.jpg");
            });
        }

        @Test
        @DisplayName("좌표가 숫자가 아니면 TourAPI 응답 예외를 던진다")
        void throwsExceptionWhenCoordinateIsInvalid() {
            assertThatThrownBy(() -> tourApiService.getNearbyTourSpots("invalid", "37.56"))
                    .isInstanceOf(TourApiException.class);
        }
    }

    private TourApiResponseDTO response(TourApiResponseDTO.TourContentDTO... contents) {
        return new TourApiResponseDTO(new TourApiResponseDTO.Response(
                new TourApiResponseDTO.Header("0000", "OK"),
                new TourApiResponseDTO.Body(
                        new TourApiResponseDTO.Items(List.of(contents)), 100, 1, contents.length)
        ));
    }

    private TourApiResponseDTO.TourContentDTO content(String category, String firstImage, String secondImage) {
        return new TourApiResponseDTO.TourContentDTO(
                126516L, 12, "보신각터", "https://example.com", "서울 종로구", "", "02-1234-5678",
                firstImage, secondImage, "126.97", "37.56", "20260819090000", null,
                "11", "110", category, null, null, "소개", null, null, null, null, null, null
        );
    }
}
