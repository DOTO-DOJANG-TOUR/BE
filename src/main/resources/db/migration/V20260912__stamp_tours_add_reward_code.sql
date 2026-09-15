ALTER TABLE stamp_tours
    ADD COLUMN reward_code VARCHAR(6);

-- 기존 행끼리 값이 겹치지 않도록 일련번호 기반으로 채운 뒤 유니크 제약을 건다
-- row_number()는 1부터 시작하므로 1을 빼서 000000~999999(VARCHAR(6) 전체 범위)를 다 쓰도록 한다
-- (그대로 두면 1,000,000번째 행에서 "1000000"이 만들어져 6자리를 넘겨 유니크 제약 충돌이 난다)
WITH numbered_tours AS (
    SELECT stamp_tour_id, row_number() OVER (ORDER BY stamp_tour_id) - 1 AS seq
    FROM stamp_tours
)
UPDATE stamp_tours
SET reward_code = lpad(numbered_tours.seq::text, 6, '0')
FROM numbered_tours
WHERE stamp_tours.stamp_tour_id = numbered_tours.stamp_tour_id;

ALTER TABLE stamp_tours
    ALTER COLUMN reward_code SET NOT NULL,
    ADD CONSTRAINT uk_stamp_tours_reward_code UNIQUE (reward_code);
