package com.doto.domain.stamp.entity;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.member.entity.Member;
import com.doto.domain.stamp.entity.enums.StampTourStatus;
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
@Table(name = "stamp_tours")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StampTour extends BaseTimeEntity {

    private static final int REQUIRED_STAMP_COUNT = 3;

    @Id
    @Tsid
    @Column(name = "stamp_tour_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "completed_stamp_count", nullable = false)
    private int completedStampCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private StampTourStatus status;

    private StampTour(Member member, Festival festival) {
        this.member = member;
        this.festival = festival;
        this.startedAt = Instant.now();
        this.status = StampTourStatus.PROGRESS;
    }

    public static StampTour create(Member member, Festival festival) {
        return new StampTour(member, festival);
    }

    public void complete() {
        this.status = StampTourStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void completeStamp() {
        if (status != StampTourStatus.PROGRESS || completedStampCount >= REQUIRED_STAMP_COUNT) {
            throw new IllegalStateException("Stamp tour cannot accept another completed stamp.");
        }

        completedStampCount++;
        if (completedStampCount == REQUIRED_STAMP_COUNT) {
            complete();
        }
    }

    public void reward() {
        this.status = StampTourStatus.REWARDED;
    }
}
