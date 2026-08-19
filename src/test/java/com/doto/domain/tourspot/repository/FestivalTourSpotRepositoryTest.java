package com.doto.domain.tourspot.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.tourspot.entity.FestivalTourSpot;
import com.doto.domain.tourspot.entity.TourSpot;
import com.doto.fixture.FestivalFixture;
import com.doto.fixture.TourSpotFixture;
import com.doto.global.config.JpaConfig;
import com.doto.global.config.TestcontainersConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, TestcontainersConfig.class})
@DisplayName("FestivalTourSpotRepository 테스트")
class FestivalTourSpotRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private FestivalTourSpotRepository festivalTourSpotRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private FestivalRepository festivalRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private TourSpotRepository tourSpotRepository;

    @Test
    @DisplayName("축제와 관광지 관계를 저장하고 축제 ID로 조회한다")
    void savesAndFindsFestivalTourSpot() {
        Festival festival = festivalRepository.saveAndFlush(FestivalFixture.create());
        TourSpot tourSpot = tourSpotRepository.saveAndFlush(TourSpotFixture.create());
        festivalTourSpotRepository.saveAndFlush(FestivalTourSpot.create(festival, tourSpot));

        assertThat(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(festival.getId(), tourSpot.getId()))
                .isTrue();
        assertThat(festivalTourSpotRepository.findAllWithTourSpotByFestivalId(festival.getId()))
                .singleElement()
                .satisfies(relation -> assertThat(relation.getTourSpot().getId()).isEqualTo(tourSpot.getId()));
    }

    @Test
    @DisplayName("같은 축제와 관광지의 관계는 중복 저장할 수 없다")
    void preventsDuplicateFestivalTourSpot() {
        Festival festival = festivalRepository.saveAndFlush(FestivalFixture.create());
        TourSpot tourSpot = tourSpotRepository.saveAndFlush(TourSpotFixture.create());
        festivalTourSpotRepository.saveAndFlush(FestivalTourSpot.create(festival, tourSpot));

        assertThatThrownBy(() -> festivalTourSpotRepository.saveAndFlush(FestivalTourSpot.create(festival, tourSpot)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
