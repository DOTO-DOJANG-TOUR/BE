package com.doto.domain.stamp.entity;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.member.entity.Member;
import com.doto.domain.stamp.entity.enums.FestivalVisitStatus;
import com.doto.global.common.BaseTimeEntity;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "festival_visits")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalVisit extends BaseTimeEntity {

    @Id
    @Tsid
    @Column(name = "festival_visit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private FestivalVisitStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    private FestivalVisit(Member member, Festival festival) {
        this.member = member;
        this.festival = festival;
        this.status = FestivalVisitStatus.VISITING;
        this.startedAt = Instant.now();
    }

    public static FestivalVisit start(Member member, Festival festival) {
        return new FestivalVisit(member, festival);
    }

    public void end(Instant endedAt) {
        if (status != FestivalVisitStatus.VISITING) {
            return;
        }

        this.status = FestivalVisitStatus.ENDED;
        this.endedAt = endedAt;
    }
}
