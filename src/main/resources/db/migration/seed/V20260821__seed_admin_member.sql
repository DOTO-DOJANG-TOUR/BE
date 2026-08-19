-- 관리자 시드 계정, 이메일은 UserResponseDTO Swagger example 참고 (초기 비밀번호 ChangeMe123!)
INSERT INTO members (member_id, nickname, status, role, created_at, updated_at)
VALUES (1, '관리자', 'ACTIVE', 'ADMIN', NOW(), NOW())
ON CONFLICT (member_id) DO NOTHING;

INSERT INTO general_auth_accounts (member_id, email, password_hash, created_at, updated_at)
VALUES (1, 'user@example.com', '$2b$10$IaeWWG8XSS8dRwpYiX.a8Oj7Q2SweisMvt.TCAJogk0BRtepBjD.y', NOW(), NOW())
ON CONFLICT (member_id) DO NOTHING;
