ALTER TABLE stamp_tours
    ADD COLUMN qr_token VARCHAR(36);

UPDATE stamp_tours
SET qr_token = md5(random()::text || clock_timestamp()::text || stamp_tour_id::text)::uuid::text
WHERE qr_token IS NULL;

ALTER TABLE stamp_tours
    ALTER COLUMN qr_token SET NOT NULL,
    ADD CONSTRAINT uk_stamp_tours_qr_token UNIQUE (qr_token);
