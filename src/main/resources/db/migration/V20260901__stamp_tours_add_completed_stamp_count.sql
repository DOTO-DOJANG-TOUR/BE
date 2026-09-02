ALTER TABLE stamp_tours
    ADD COLUMN completed_stamp_count INTEGER NOT NULL DEFAULT 0;

UPDATE stamp_tours stamp_tour
SET completed_stamp_count = completed_stamps.count
FROM (
    SELECT stamp_tour_id, COUNT(*) AS count
    FROM stamps
    WHERE status = 'COMPLETED'
    GROUP BY stamp_tour_id
) completed_stamps
WHERE stamp_tour.stamp_tour_id = completed_stamps.stamp_tour_id;

ALTER TABLE stamp_tours
    ADD CONSTRAINT chk_stamp_tours_completed_stamp_count
    CHECK (completed_stamp_count BETWEEN 0 AND 3);
