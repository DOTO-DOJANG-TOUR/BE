-- 축제 검색(제목 유사도)용 pg_trgm 확장 및 트라이그램 인덱스
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_festivals_title_trgm ON festivals USING GIN (title gin_trgm_ops);
