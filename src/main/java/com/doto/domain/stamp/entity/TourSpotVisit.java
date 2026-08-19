package com.doto.domain.stamp.entity;

import com.doto.domain.member.entity.Member;
import com.doto.domain.tourspot.entity.TourSpot;
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
@Table(name = "tour_spot_visits")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourSpotVisit extends BaseTimeEntity {

    @Id
    @Tsid
    @Column(name = "tour_spot_visit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_spot_id", nullable = false)
    private TourSpot tourSpot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private TourSpotVisitStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    private TourSpotVisit(Member member, TourSpot tourSpot, Instant expiresAt) {
        if (!expiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Visit expiration time must be in the future.");
        }

        this.member = member;
        this.tourSpot = tourSpot;
        this.status = TourSpotVisitStatus.VISITING;
        this.expiresAt = expiresAt;
    }

    public static TourSpotVisit start(Member member, TourSpot tourSpot, Instant expiresAt) {
        return new TourSpotVisit(member, tourSpot, expiresAt);
    }

    public boolean isExpiredAt(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public void expire(Instant expiredAt) {
        if (status != TourSpotVisitStatus.VISITING) {
            return;
        }
        if (expiredAt.isBefore(expiresAt)) {
            throw new IllegalArgumentException("Visit cannot expire before its expiration time.");
        }

        this.status = TourSpotVisitStatus.EXPIRED;
        this.endedAt = expiredAt;
    }

    public void end(Instant endedAt) {
        if (status != TourSpotVisitStatus.VISITING) {
            return;
        }

        this.status = TourSpotVisitStatus.ENDED;
        this.endedAt = endedAt;
    }
}
