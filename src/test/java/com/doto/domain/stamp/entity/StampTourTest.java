package com.doto.domain.stamp.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.entity.enums.Region;
import com.doto.domain.member.entity.Member;
import java.time.Instant;

import com.doto.domain.stamp.entity.enums.StampTourStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

class StampTourTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Nested
    class 생성 {

        @Test
        void 투어를_시작하면_PROGRESS_상태와_시작_시각을_기록한다() {
            StampTour stampTour = createStampTour();

            assertThat(stampTour.getStatus()).isEqualTo(StampTourStatus.PROGRESS);
            assertThat(stampTour.getStartedAt()).isNotNull();
            assertThat(stampTour.getQrToken()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
            assertThat(stampTour.getRewardCode()).matches("^[0-9]{6}$");
        }

        @Test
        void 같은_회원이_같은_축제의_새_투어를_시작하면_서로_다른_QR_토큰을_발급한다() {
            Member member = Member.register("홍길동");
            Festival festival = createFestival();
            StampTour firstTour = StampTour.create(member, festival);
            StampTour secondTour = StampTour.create(member, festival);

            assertThat(firstTour.getQrToken()).isNotEqualTo(secondTour.getQrToken());
        }

        @Test
        void 서로_다른_회원의_투어에는_서로_다른_QR_토큰을_발급한다() {
            Festival festival = createFestival();
            StampTour firstMemberTour = StampTour.create(Member.register("홍길동"), festival);
            StampTour secondMemberTour = StampTour.create(Member.register("김도토"), festival);

            assertThat(firstMemberTour.getQrToken()).isNotEqualTo(secondMemberTour.getQrToken());
        }
    }

    @Nested
    class 완료 {

        @Test
        void 투어를_완료하면_COMPLETED_상태와_완료_시각을_기록한다() {
            StampTour stampTour = createStampTour();

            stampTour.complete();

            assertThat(stampTour.getStatus()).isEqualTo(StampTourStatus.COMPLETED);
            assertThat(stampTour.getCompletedAt()).isNotNull();
        }
    }

    @Nested
    class 보상 {

        @Test
        void 보상하면_REWARDED_상태가_된다() {
            StampTour stampTour = createStampTour();

            stampTour.reward();

            assertThat(stampTour.getStatus()).isEqualTo(StampTourStatus.REWARDED);
        }
    }

    private StampTour createStampTour() {
        return StampTour.create(Member.register("홍길동"), createFestival());
    }

    private Festival createFestival() {
        return Festival.create(
                2515245L, "도토 축제", null, null, null, null, null, null, null, null, null, null, null, null, Region.서울특별시, "680",
                Instant.parse("2026-08-15T00:00:00Z"), Instant.parse("2026-08-20T00:00:00Z"),
                GEOMETRY_FACTORY.createPoint(new Coordinate(127.0276, 37.4979))
        );
    }
}
