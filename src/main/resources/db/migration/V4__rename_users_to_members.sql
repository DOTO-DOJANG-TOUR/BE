ALTER TABLE users RENAME TO members;
ALTER TABLE members RENAME COLUMN user_id TO member_id;
ALTER TABLE members RENAME CONSTRAINT pk_users TO pk_members;

ALTER TABLE general_auth_accounts RENAME COLUMN user_id TO member_id;
ALTER TABLE general_auth_accounts RENAME CONSTRAINT fk_general_auth_accounts_user TO fk_general_auth_accounts_member;

ALTER TABLE social_auth_accounts RENAME COLUMN user_id TO member_id;
ALTER TABLE social_auth_accounts RENAME CONSTRAINT fk_social_auth_accounts_user TO fk_social_auth_accounts_member;
ALTER INDEX idx_social_auth_accounts_user_id RENAME TO idx_social_auth_accounts_member_id;

ALTER TABLE refresh_tokens RENAME COLUMN user_id TO member_id;
ALTER TABLE refresh_tokens RENAME CONSTRAINT fk_refresh_tokens_user TO fk_refresh_tokens_member;
ALTER INDEX idx_refresh_tokens_user_id RENAME TO idx_refresh_tokens_member_id;
