ALTER TABLE festivals ADD COLUMN region VARCHAR(20);
ALTER TABLE festivals ADD COLUMN region_group VARCHAR(20);
ALTER TABLE festivals DROP COLUMN l_dong_regn_cd;
ALTER TABLE festivals DROP COLUMN l_dong_signgu_cd;
