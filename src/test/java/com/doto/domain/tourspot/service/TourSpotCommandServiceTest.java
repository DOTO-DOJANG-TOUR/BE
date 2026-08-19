package com.doto.domain.tourspot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.festival.exception.FestivalException;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.stamp.dto.TourSpotItemResponseDTO;
import com.doto.domain.tourspot.entity.FestivalTourSpot;
import com.doto.domain.tourspot.entity.TourSpot;
import com.doto.domain.tourspot.repository.FestivalTourSpotRepository;
import com.doto.domain.tourspot.repository.TourSpotRepository;
import com.doto.fixture.FestivalFixture;
import com.doto.fixture.TourSpotFixture;
import java.util.List;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("TourSpotCommandService 단위 테스트")
class TourSpotCommandServiceTest {

    @Mock
    private TourSpotRepository tourSpotRepository;

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private FestivalTourSpotRepository festivalTourSpotRepository;

    @InjectMocks
    private TourSpotCommandService tourSpotCommandService;

    @Nested
    @DisplayName("관광지 저장")
    class SaveTourSpots {

        @Test
        @DisplayName("축제가 없으면 FESTIVAL_NOT_FOUND 예외를 던진다")
        void throwsExceptionWhenFestivalDoesNotExist() {
            Long festivalContentId = 2515245L;
            given(festivalRepository.findByContentId(festivalContentId)).willReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> tourSpotCommandService.saveTourSpots(
                    festivalContentId, List.of(TourSpotFixture.createResponseDTO())))
                    .isInstanceOf(FestivalException.class)
                    .satisfies(exception -> assertThat(((FestivalException) exception).getErrorCode())
                            .isEqualTo(FestivalErrorCode.FESTIVAL_NOT_FOUND));

            then(tourSpotRepository).should(never()).saveAll(anyList());
            then(festivalTourSpotRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("새 관광지를 만들고 축제와의 관계를 저장한다")
        void createsTourSpotAndFestivalRelationWhenTourSpotDoesNotExist() {
            Festival festival = festivalWithId(1L);
            TourSpotItemResponseDTO dto = TourSpotFixture.createResponseDTO(125405L);
            given(festivalRepository.findByContentId(festival.getContentId())).willReturn(java.util.Optional.of(festival));
            given(tourSpotRepository.findByContentId(dto.contentId())).willReturn(java.util.Optional.empty());
            givenSaveAllReturnsInput();

            tourSpotCommandService.saveTourSpots(festival.getContentId(), List.of(dto));

            then(tourSpotRepository).should().saveAll(org.mockito.ArgumentMatchers.argThat(tourSpots -> {
                TourSpot createdTourSpot = tourSpots.iterator().next();
                return createdTourSpot.getContentId().equals(dto.contentId())
                        && createdTourSpot.getTitle().equals(dto.title());
            }));
            then(festivalTourSpotRepository).should().saveAll(org.mockito.ArgumentMatchers.argThat(relations -> {
                FestivalTourSpot relation = relations.iterator().next();
                return relation.getFestival() == festival
                        && relation.getTourSpot().getContentId().equals(dto.contentId());
            }));
        }

        @Test
        @DisplayName("기존 관광지와 이미 연결되어 있으면 정보만 갱신하고 관계를 저장하지 않는다")
        void updatesTourSpotOnlyWhenRelationAlreadyExists() {
            Festival festival = festivalWithId(1L);
            TourSpot existingTourSpot = tourSpotWithId(2L, 125405L);
            TourSpotItemResponseDTO dto = TourSpotFixture.createResponseDTO(125405L);
            given(festivalRepository.findByContentId(festival.getContentId())).willReturn(java.util.Optional.of(festival));
            given(tourSpotRepository.findByContentId(dto.contentId())).willReturn(java.util.Optional.of(existingTourSpot));
            given(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(1L, 2L)).willReturn(true);
            givenSaveAllReturnsInput();

            tourSpotCommandService.saveTourSpots(festival.getContentId(), List.of(dto));

            assertThat(existingTourSpot.getTitle()).isEqualTo(dto.title());
            assertThat(existingTourSpot.getAddress()).isEqualTo(dto.address());
            assertThat(existingTourSpot.getImageUrl()).isEqualTo(dto.imageUrl());
            then(tourSpotRepository).should().saveAll(List.of(existingTourSpot));
            then(festivalTourSpotRepository).should(never()).saveAll(anyList());
        }

        @Test
        @DisplayName("기존 관광지가 축제와 연결되어 있지 않으면 관계만 저장한다")
        void createsOnlyFestivalRelationWhenExistingTourSpotIsNotLinked() {
            Festival festival = festivalWithId(1L);
            TourSpot existingTourSpot = tourSpotWithId(2L, 125405L);
            TourSpotItemResponseDTO dto = TourSpotFixture.createResponseDTO(125405L);
            given(festivalRepository.findByContentId(festival.getContentId())).willReturn(java.util.Optional.of(festival));
            given(tourSpotRepository.findByContentId(dto.contentId())).willReturn(java.util.Optional.of(existingTourSpot));
            given(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(1L, 2L)).willReturn(false);
            givenSaveAllReturnsInput();

            tourSpotCommandService.saveTourSpots(festival.getContentId(), List.of(dto));

            then(festivalTourSpotRepository).should().saveAll(org.mockito.ArgumentMatchers.argThat(relations -> {
                FestivalTourSpot relation = relations.iterator().next();
                return relation.getFestival() == festival && relation.getTourSpot() == existingTourSpot;
            }));
        }

        @Test
        @DisplayName("동일한 contentId가 중복되면 관광지와 관계를 한 번만 저장한다")
        void savesDuplicateContentIdOnlyOnce() {
            Festival festival = festivalWithId(1L);
            TourSpotItemResponseDTO dto = TourSpotFixture.createResponseDTO(125405L);
            given(festivalRepository.findByContentId(festival.getContentId())).willReturn(java.util.Optional.of(festival));
            given(tourSpotRepository.findByContentId(dto.contentId())).willReturn(java.util.Optional.empty());
            givenSaveAllReturnsInput();

            tourSpotCommandService.saveTourSpots(festival.getContentId(), List.of(dto, dto));

            then(tourSpotRepository).should().findByContentId(dto.contentId());
            then(tourSpotRepository).should().saveAll(org.mockito.ArgumentMatchers.argThat(tourSpots ->
                    StreamSupport.stream(tourSpots.spliterator(), false).count() == 1));
            then(festivalTourSpotRepository).should().saveAll(org.mockito.ArgumentMatchers.argThat(relations ->
                    StreamSupport.stream(relations.spliterator(), false).count() == 1));
        }
    }

    private Festival festivalWithId(Long id) {
        Festival festival = FestivalFixture.create();
        ReflectionTestUtils.setField(festival, "id", id);
        return festival;
    }

    private TourSpot tourSpotWithId(Long id, Long contentId) {
        TourSpot tourSpot = TourSpotFixture.create(contentId);
        ReflectionTestUtils.setField(tourSpot, "id", id);
        return tourSpot;
    }

    private void givenSaveAllReturnsInput() {
        given(tourSpotRepository.saveAll(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));
    }
}
