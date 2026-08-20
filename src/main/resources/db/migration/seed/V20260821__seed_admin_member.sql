-- 관리자 시드 계정 (초기 비밀번호 stringst)
INSERT INTO members (member_id, nickname, status, role, created_at, updated_at)
VALUES (1, '관리자', 'ACTIVE', 'ADMIN', NOW(), NOW())
ON CONFLICT (member_id) DO NOTHING;

INSERT INTO general_auth_accounts (member_id, email, password_hash, created_at, updated_at)
SELECT 1, 'user@example.com', '$2b$10$xD9ALWfW32tEnCfw9aIk3OesGFNfUnWoFZhQ1HUwxDlaN3EI1itCC', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM general_auth_accounts WHERE email = 'user@example.com'
)
ON CONFLICT (member_id) DO NOTHING;
