CREATE TABLE festival_visits (
    festival_visit_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    festival_id BIGINT NOT NULL,
    status VARCHAR(10) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_festival_visits PRIMARY KEY (festival_visit_id),
    CONSTRAINT fk_festival_visits_member FOREIGN KEY (member_id) REFERENCES members (member_id),
    CONSTRAINT fk_festival_visits_festival FOREIGN KEY (festival_id) REFERENCES festivals (festival_id)
);

CREATE INDEX idx_festival_visits_festival_id ON festival_visits (festival_id);
CREATE UNIQUE INDEX uk_festival_visits_member_visiting
    ON festival_visits (member_id)
    WHERE status = 'VISITING';
