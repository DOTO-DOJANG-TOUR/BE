package com.doto.domain.stamp.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.domain.stamp.repository.FestivalVisitRepository;
import com.doto.domain.stamp.repository.StampTourRepository;
import com.doto.domain.tourspot.repository.FestivalTourSpotRepository;
import com.doto.fixture.FestivalFixture;
import com.doto.fixture.MemberFixture;
import com.doto.global.config.JpaConfig;
import com.doto.global.config.TestcontainersConfig;
import jakarta.persistence.EntityManagerFactory;
import java.time.Clock;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

// 도장 투어 시작(startStampTour)이 실제로 몇 번 쿼리를 날리는지 회귀 검증하기 위한 테스트
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, TestcontainersConfig.class})
class StampTourServiceQueryCountTest {

    @Autowired
    private FestivalRepository festivalRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private StampTourRepository stampTourRepository;
    @Autowired
    private FestivalVisitRepository festivalVisitRepository;
    @Autowired
    private FestivalTourSpotRepository festivalTourSpotRepository;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private TestEntityManager testEntityManager;

    private Statistics stats;

    @BeforeEach
    void setUp() {
        stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
    }

    @Test
    void 투어_시작은_쿼리를_5번만_실행한다() {
        Member member = memberRepository.saveAndFlush(MemberFixture.create());
        Festival festival = festivalRepository.saveAndFlush(FestivalFixture.create());

        StampTourService stampTourService = new StampTourService(
                festivalRepository,
                memberRepository,
                stampTourRepository,
                festivalVisitRepository,
                festivalTourSpotRepository,
                Clock.systemUTC()
        );

        stats.clear(); // 위 저장 쿼리는 측정에서 제외

        stampTourService.startStampTour(member.getId(), festival.getId());
        testEntityManager.flush(); // 지연된 INSERT까지 강제로 내보내서 실제 개수를 확인

        assertThat(stats.getPrepareStatementCount()).isEqualTo(5);
    }
}
