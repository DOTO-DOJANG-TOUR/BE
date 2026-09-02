package com.doto.domain.tourex.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.doto.domain.tourex.enums.TourApiCategory;
import com.doto.domain.tourex.client.TourApiClient;
import com.doto.domain.tourex.dto.FestivalApiResponseDTO;
import com.doto.domain.tourex.dto.FestivalIntroApiResponseDTO;
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
            assertThat(result.tourSpotCategory()).isEqualTo(TourApiCategory.문화관광);
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
                assertThat(tourSpot.tourSpotCategory()).isEqualTo(TourApiCategory.역사관광);
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

    @Nested
    @DisplayName("축제 상세 조회")
    class GetFestivalInfo {

        @Test
        @DisplayName("TourAPI 콘텐츠와 소개 정보를 축제 응답으로 변환한다")
        void mapsFestivalInfo() {
            given(tourApiClient.getContentDetail(126516L))
                    .willReturn(response(festivalContent("041-730-2971,3", "https://example.com", "EV010100")));
            given(tourApiClient.getFestivalIntro(126516L)).willReturn(festivalIntro(
                    "18:00~22:00", "없음", "무료", null
            ));

            FestivalApiResponseDTO result = tourApiService.getFestivalInfo(126516L, "문화관광축제");

            assertThat(result.title()).isEqualTo("강경 국가유산야행");
            assertThat(result.phone()).isEqualTo("041-730-2971,3");
            assertThat(result.homepageUrl()).isEqualTo("https://example.com");
            assertThat(result.operationHours()).isEqualTo("18:00~22:00");
            assertThat(result.holiday()).isEqualTo("없음");
            assertThat(result.fee()).isEqualTo("무료");
            assertThat(result.festivalType()).isEqualTo("문화관광축제");
            assertThat(result.category()).isEqualTo("문화관광");
        }

        @Test
        @DisplayName("TourAPI가 빈 문자열이나 공백만 내려준 필드는 null로 저장된다")
        void blankFieldsAreNormalizedToNull() {
            given(tourApiClient.getContentDetail(126516L))
                    .willReturn(response(festivalContent("  ", "", "  ")));
            given(tourApiClient.getFestivalIntro(126516L)).willReturn(festivalIntro(
                    " ", "", "   ", "  "
            ));

            FestivalApiResponseDTO result = tourApiService.getFestivalInfo(126516L, "  ");

            assertThat(result.phone()).isNull();
            assertThat(result.homepageUrl()).isNull();
            assertThat(result.operationHours()).isNull();
            assertThat(result.holiday()).isNull();
            assertThat(result.fee()).isNull();
            assertThat(result.festivalType()).isNull();
            // category는 다른 필드와 달리 blank/미매핑 코드여도 null이 아니라 "기타"로 저장된다
            assertThat(result.category()).isEqualTo("기타");
        }

        @Test
        @DisplayName("EV01 소분류 6개에 없는 lclsSystm3 코드는 기타로 저장된다")
        void unmappedCategoryCodeFallsBackToEtc() {
            given(tourApiClient.getContentDetail(126516L))
                    .willReturn(response(festivalContent("041-730-2971,3", "https://example.com", "EV020100")));
            given(tourApiClient.getFestivalIntro(126516L)).willReturn(festivalIntro(
                    "18:00~22:00", "없음", "무료", null
            ));

            FestivalApiResponseDTO result = tourApiService.getFestivalInfo(126516L, "공연");

            assertThat(result.category()).isEqualTo("기타");
        }
    }

    // tel, homepage, lclsSystem3 외 나머지 필드는 이 테스트들에서 의미가 없어 고정값을 사용한다
    private TourApiResponseDTO.TourContentDTO festivalContent(String tel, String homepage, String lclsSystem3) {
        return new TourApiResponseDTO.TourContentDTO(
                126516L, 15, "강경 국가유산야행", homepage, "충청남도 논산시 강경읍 중앙리", null, tel,
                "https://tong.visitkorea.or.kr/cms/resource/94/3519794_image2_1.jpg", null,
                "127.02", "36.16", null, null, "44", "44230", "EV", "EV01", lclsSystem3,
                "강경 국가유산 야행은 보존에 치중하던 기존 틀에서 벗어난다", null, null, null, null, null, null
        );
    }

    // usetimefestival, restdate, usefee, eventhomepage 외 나머지 필드는 이 테스트들에서 의미가 없어 고정값을 사용한다
    private FestivalIntroApiResponseDTO festivalIntro(
            String usetimefestival, String restdate, String usefee, String eventhomepage
    ) {
        return new FestivalIntroApiResponseDTO(new FestivalIntroApiResponseDTO.Response(
                new FestivalIntroApiResponseDTO.Header("0000", "OK"),
                new FestivalIntroApiResponseDTO.Body(new FestivalIntroApiResponseDTO.Items(List.of(
                        new FestivalIntroApiResponseDTO.FestivalIntroDTO(
                                "20261010", "20261011", usetimefestival, null, null,
                                restdate, usefee, null, null, null,
                                null, eventhomepage, null, null, null,
                                null, null, null, null, null, null
                        )
                )))
        ));
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
