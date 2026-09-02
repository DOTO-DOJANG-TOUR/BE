package com.doto.fixture;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.member.entity.Member;
import com.doto.domain.stamp.entity.StampTour;
import org.springframework.test.util.ReflectionTestUtils;

public final class StampTourFixture {

    private StampTourFixture() {
    }

    public static StampTour create(Member member, Festival festival) {
        StampTour stampTour = StampTour.create(member, festival);
        ReflectionTestUtils.setField(stampTour, "id", 1L);
        return stampTour;
    }
}
