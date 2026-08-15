package com.doto.domain.stamp.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.doto.domain.festival.entity.TourSpot;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StampTest {

    @Nested
    class 방문_시작 {

        @Test
        void 생성하면_VISITING_상태로_시작한다() {
            Stamp stamp = Stamp.create(mock(StampTour.class), mock(TourSpot.class));

            assertThat(stamp.getStatus()).isEqualTo(StampStatus.VISITING);
            assertThat(stamp.getStartedAt()).isNotNull();
            assertThat(stamp.getCompletedAt()).isNull();
        }
    }

    @Nested
    class 방문_완료 {

        @Test
        void 완료하면_COMPLETED_상태와_완료_시각을_기록한다() {
            Stamp stamp = Stamp.create(mock(StampTour.class), mock(TourSpot.class));

            stamp.complete();

            assertThat(stamp.getStatus()).isEqualTo(StampStatus.COMPLETED);
            assertThat(stamp.getCompletedAt()).isNotNull();
        }
    }

    @Nested
    class 방문_취소 {

        @Test
        void 취소하면_CANCELED_상태가_된다() {
            Stamp stamp = Stamp.create(mock(StampTour.class), mock(TourSpot.class));

            stamp.cancel();

            assertThat(stamp.getStatus()).isEqualTo(StampStatus.CANCELED);
        }
    }
}
