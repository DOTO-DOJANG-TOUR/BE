CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE festivals (
    festival_id BIGINT NOT NULL,
    title VARCHAR(100),
    summary TEXT,
    homepage_url TEXT,
    category VARCHAR(50),
    phone VARCHAR(20),
    address VARCHAR(100),
    play_time VARCHAR(20),
    rest_date VARCHAR(50),
    use_fee VARCHAR(50),
    parking VARCHAR(50),
    parking_fee VARCHAR(50),
    area_code VARCHAR(10) NOT NULL,
    region_code VARCHAR(10),
    sigungu_code VARCHAR(10) NOT NULL,
    event_start_date TIMESTAMPTZ NOT NULL,
    event_end_date TIMESTAMPTZ NOT NULL,
    location geography(Point, 4326) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_festivals PRIMARY KEY (festival_id)
);

CREATE INDEX idx_festivals_location ON festivals USING GIST (location);

CREATE TABLE tour_spots (
    tour_spot_id BIGINT NOT NULL,
    festival_id BIGINT NOT NULL,
    title VARCHAR(100),
    category VARCHAR(50),
    location geography(Point, 4326),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_tour_spots PRIMARY KEY (tour_spot_id),
    CONSTRAINT fk_tour_spots_festival FOREIGN KEY (festival_id) REFERENCES festivals (festival_id)
);

CREATE INDEX idx_tour_spots_festival_id ON tour_spots (festival_id);
CREATE INDEX idx_tour_spots_location ON tour_spots USING GIST (location);

CREATE TABLE stamp_tours (
    stamp_tour_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    festival_id BIGINT NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    status VARCHAR(10) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_stamp_tours PRIMARY KEY (stamp_tour_id),
    CONSTRAINT fk_stamp_tours_member FOREIGN KEY (member_id) REFERENCES members (member_id),
    CONSTRAINT fk_stamp_tours_festival FOREIGN KEY (festival_id) REFERENCES festivals (festival_id)
);

CREATE INDEX idx_stamp_tours_member_id ON stamp_tours (member_id);
CREATE INDEX idx_stamp_tours_festival_id ON stamp_tours (festival_id);

CREATE TABLE stamps (
    stamp_id BIGINT NOT NULL,
    stamp_tour_id BIGINT NOT NULL,
    tour_spot_id BIGINT NOT NULL,
    status VARCHAR(10) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_stamps PRIMARY KEY (stamp_id),
    CONSTRAINT fk_stamps_stamp_tour FOREIGN KEY (stamp_tour_id) REFERENCES stamp_tours (stamp_tour_id),
    CONSTRAINT fk_stamps_tour_spot FOREIGN KEY (tour_spot_id) REFERENCES tour_spots (tour_spot_id),
    CONSTRAINT uk_stamps_stamp_tour_tour_spot UNIQUE (stamp_tour_id, tour_spot_id)
);

CREATE INDEX idx_stamps_stamp_tour_id ON stamps (stamp_tour_id);
CREATE UNIQUE INDEX uk_stamps_stamp_tour_visiting ON stamps (stamp_tour_id) WHERE status = 'VISITING';
