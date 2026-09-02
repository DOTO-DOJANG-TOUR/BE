package com.doto.domain.stamp.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.doto.domain.member.entity.Member;
import com.doto.domain.festival.entity.Festival;
import com.doto.domain.stamp.entity.enums.TourSpotVisitStatus;
import com.doto.domain.tourspot.entity.TourSpot;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TourSpotVisitTest {

    @Nested
    class 방문_시작 {

        @Test
        void 회원과_관광지_및_만료_시각으로_VISITING_상태의_방문을_생성한다() {
            Member member = Mockito.mock(Member.class);
            Festival festival = Mockito.mock(Festival.class);
            TourSpot tourSpot = Mockito.mock(TourSpot.class);
            Instant expiresAt = Instant.now().plusSeconds(7 * 60);

            TourSpotVisit visit = TourSpotVisit.start(member, festival, tourSpot, expiresAt);

            assertThat(visit.getMember()).isEqualTo(member);
            assertThat(visit.getFestival()).isEqualTo(festival);
            assertThat(visit.getTourSpot()).isEqualTo(tourSpot);
            assertThat(visit.getStatus()).isEqualTo(TourSpotVisitStatus.VISITING);
            assertThat(visit.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(visit.getEndedAt()).isNull();
        }
    }

    @Nested
    class 방문_종료 {

        @Test
        void 만료하면_EXPIRED_상태와_종료_시각을_기록한다() {
            TourSpotVisit visit = createVisitingVisit();
            Instant expiredAt = visit.getExpiresAt();

            visit.expire(expiredAt);

            assertThat(visit.getStatus()).isEqualTo(TourSpotVisitStatus.EXPIRED);
            assertThat(visit.getEndedAt()).isEqualTo(expiredAt);
        }

        @Test
        void 만료_시각_전에는_만료할_수_없다() {
            TourSpotVisit visit = createVisitingVisit();

            assertThatThrownBy(() -> visit.expire(visit.getExpiresAt().minusSeconds(1)))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(visit.getStatus()).isEqualTo(TourSpotVisitStatus.VISITING);
        }

        @Test
        void 사용자가_종료하면_ENDED_상태와_종료_시각을_기록한다() {
            TourSpotVisit visit = createVisitingVisit();
            Instant endedAt = Instant.now();

            visit.end(endedAt);

            assertThat(visit.getStatus()).isEqualTo(TourSpotVisitStatus.ENDED);
            assertThat(visit.getEndedAt()).isEqualTo(endedAt);
        }
    }

    @Test
    void 만료_시각이_현재보다_이르면_만료된_방문이다() {
        TourSpotVisit visit = createVisitingVisit();

        assertThat(visit.isExpiredAt(visit.getExpiresAt())).isTrue();
        assertThat(visit.isExpiredAt(visit.getExpiresAt().minusSeconds(1))).isFalse();
    }

    private TourSpotVisit createVisitingVisit() {
        return TourSpotVisit.start(
                Mockito.mock(Member.class),
                Mockito.mock(Festival.class),
                Mockito.mock(TourSpot.class),
                Instant.now().plusSeconds(420)
        );
    }
}
