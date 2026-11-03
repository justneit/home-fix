-- DEVELOPMENT ONLY (loaded by the 'dev' profile). Fictional demo accounts.
-- Password for every account: HomeFix@123
SET @pwd = '{bcrypt}$2b$10$7zmPep/FSbUJRqBojTflCOnRNCpfweFiQ0ig7Kw/xQTYNie.wG1Pq';

INSERT INTO users (email, password_hash, full_name, phone, role, status, created_at, updated_at)
VALUES ('admin@homefix.test', @pwd, 'Quản trị viên', '0900000001', 'ADMIN', 'ACTIVE', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('khachhang@homefix.test', @pwd, 'Nguyễn Văn An', '0900000002', 'CUSTOMER', 'ACTIVE', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('thosua@homefix.test', @pwd, 'Trần Minh Thợ', '0900000003', 'WORKER', 'ACTIVE', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6))
AS seed
ON DUPLICATE KEY UPDATE password_hash = seed.password_hash, full_name = seed.full_name;

INSERT INTO worker_profiles (user_id, verification_status, available, created_at, updated_at)
SELECT id, 'VERIFIED', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6) FROM users WHERE email = 'thosua@homefix.test'
ON DUPLICATE KEY UPDATE verification_status = 'VERIFIED';
