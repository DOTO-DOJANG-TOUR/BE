
INSERT INTO members (member_id, nickname, status, role, created_at, updated_at)
VALUES (1, '관리자', 'ACTIVE', 'ADMIN', NOW(), NOW())
ON CONFLICT (member_id) DO NOTHING;

INSERT INTO general_auth_accounts (
    member_id,
    email,
    password_hash,
    created_at,
    updated_at
)
VALUES (1,'user@example.com',
        '$2b$10$iAkITcfb7aQ9ejv82YShPuNhgwNKDwuZG8GIWjZZkUbQFgEMjCGeq',NOW(),NOW())
    ON CONFLICT DO NOTHING;
