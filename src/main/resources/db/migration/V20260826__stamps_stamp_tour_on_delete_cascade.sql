ALTER TABLE stamps
    DROP CONSTRAINT fk_stamps_stamp_tour;

ALTER TABLE stamps
    ADD CONSTRAINT fk_stamps_stamp_tour FOREIGN KEY (stamp_tour_id)
        REFERENCES stamp_tours (stamp_tour_id) ON DELETE CASCADE;
