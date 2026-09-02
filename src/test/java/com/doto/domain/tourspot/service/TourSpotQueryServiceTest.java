package com.doto.domain.tourspot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.stamp.dto.StampTourSpotItemResponseDTO;
import com.doto.domain.tourspot.entity.FestivalTourSpot;
import com.doto.domain.tourspot.entity.TourSpot;
import com.doto.domain.tourspot.exception.TourException;
import com.doto.domain.tourspot.repository.FestivalTourSpotRepository;
import com.doto.domain.tourspot.repository.TourSpotRepository;
import com.doto.fixture.FestivalFixture;
import com.doto.fixture.TourSpotFixture;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("TourSpotQueryService 단위 테스트")
class TourSpotQueryServiceTest {

    @Mock
    private FestivalTourSpotRepository festivalTourSpotRepository;

    @Mock
    private TourSpotRepository tourSpotRepository;

    @InjectMocks
    private TourSpotQueryService tourSpotQueryService;

    @Nested
    @DisplayName("관광지 검색")
    class SearchTourSpots {

        @Test
        @DisplayName("키워드를 공백 제거 후 제목 검색에 사용하고 거리 문자열을 반환한다")
        void searchesWithTrimmedKeyword() {
            FestivalTourSpot relation = festivalTourSpot(1L, 2L, BigDecimal.valueOf(1_200));
            given(festivalTourSpotRepository.searchAllWithTourSpotByFestivalIdAndKeyword(1L, "광장"))
                    .willReturn(List.of(relation));

            List<StampTourSpotItemResponseDTO> result = tourSpotQueryService.searchTourSpots(1L, "  광장  ");

            assertThat(result).singleElement().satisfies(item -> {
                assertThat(item.tourSpotId()).isEqualTo("2");
                assertThat(item.title()).isEqualTo("도토 광장");
                assertThat(item.distance()).isEqualTo("1.2km");
            });
        }

        @Test
        @DisplayName("키워드가 비어 있으면 전체 조회 쿼리를 사용한다")
        void searchesAllWhenKeywordIsBlank() {
            given(festivalTourSpotRepository.findAllWithTourSpotByFestivalId(1L)).willReturn(List.of());

            List<StampTourSpotItemResponseDTO> result = tourSpotQueryService.searchTourSpots(1L, " ");

            assertThat(result).isEmpty();
            then(festivalTourSpotRepository).should().findAllWithTourSpotByFestivalId(1L);
            then(festivalTourSpotRepository).should(org.mockito.Mockito.never())
                    .searchAllWithTourSpotByFestivalIdAndKeyword(1L, null);
        }
    }

    @Nested
    @DisplayName("관광지 상세 조회")
    class GetTourSpotDetail {

        @Test
        @DisplayName("축제에 연결된 관광지의 상세 정보를 반환한다")
        void returnsTourSpotDetail() {
            TourSpot tourSpot = TourSpotFixture.create();
            ReflectionTestUtils.setField(tourSpot, "id", 2L);
            given(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(1L, 2L)).willReturn(true);
            given(tourSpotRepository.findById(2L)).willReturn(Optional.of(tourSpot));

            var result = tourSpotQueryService.getTourSpotDetail(1L, 2L);

            assertThat(result.tourSpotId()).isEqualTo("2");
            assertThat(result.title()).isEqualTo("도토 광장");
        }

        @Test
        @DisplayName("축제에 연결되지 않은 관광지는 찾을 수 없다고 처리한다")
        void throwsExceptionWhenTourSpotIsNotInFestival() {
            given(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(1L, 2L)).willReturn(false);

            assertThatThrownBy(() -> tourSpotQueryService.getTourSpotDetail(1L, 2L))
                    .isInstanceOf(TourException.class);
        }
    }

    private FestivalTourSpot festivalTourSpot(Long festivalId, Long tourSpotId, BigDecimal distanceMeters) {
        Festival festival = FestivalFixture.create();
        TourSpot tourSpot = TourSpotFixture.create();
        ReflectionTestUtils.setField(festival, "id", festivalId);
        ReflectionTestUtils.setField(tourSpot, "id", tourSpotId);
        return FestivalTourSpot.create(festival, tourSpot, distanceMeters);
    }
}
