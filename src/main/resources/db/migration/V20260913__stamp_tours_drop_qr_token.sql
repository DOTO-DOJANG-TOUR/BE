-- QR코드에는 이제 reward_code(6자리 숫자)를 그대로 인코딩하므로 qr_token은 더 이상 쓰지 않는다
ALTER TABLE stamp_tours
    DROP CONSTRAINT uk_stamp_tours_qr_token;

ALTER TABLE stamp_tours
    DROP COLUMN qr_token;
