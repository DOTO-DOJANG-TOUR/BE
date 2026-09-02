package com.doto.fixture;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.member.entity.Member;
import com.doto.domain.stamp.entity.FestivalVisit;
import org.springframework.test.util.ReflectionTestUtils;

public final class FestivalVisitFixture {

    private FestivalVisitFixture() {
    }

    public static FestivalVisit create(Member member, Festival festival) {
        FestivalVisit festivalVisit = FestivalVisit.start(member, festival);
        ReflectionTestUtils.setField(festivalVisit, "id", 1L);
        return festivalVisit;
    }
}
