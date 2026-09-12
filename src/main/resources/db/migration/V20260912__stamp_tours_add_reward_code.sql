ALTER TABLE stamp_tours
    ADD COLUMN reward_code VARCHAR(6);

UPDATE stamp_tours
SET reward_code = lpad((floor(random() * 1000000))::int::text, 6, '0')
WHERE reward_code IS NULL;

ALTER TABLE stamp_tours
    ALTER COLUMN reward_code SET NOT NULL;
