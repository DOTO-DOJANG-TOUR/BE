ALTER TABLE tour_spot_visits
    ADD COLUMN festival_id BIGINT NOT NULL,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT fk_tour_spot_visits_festival
        FOREIGN KEY (festival_id) REFERENCES festivals (festival_id);
