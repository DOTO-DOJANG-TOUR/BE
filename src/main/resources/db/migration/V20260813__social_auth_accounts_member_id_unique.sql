DROP INDEX IF EXISTS idx_social_auth_accounts_member_id;

ALTER TABLE social_auth_accounts
    ADD CONSTRAINT uk_social_auth_accounts_member_id UNIQUE (member_id);
