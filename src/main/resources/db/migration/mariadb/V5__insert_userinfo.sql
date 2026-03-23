-- UserInfo 테스트 계정 삽입
--
-- password 컬럼은 BCryptPasswordEncoder(strength=10) 인코딩 값이어야 합니다.
-- UserInfoUserDetailsService.addUser()는 API 호출 시 자동으로 인코딩하지만,
-- Flyway SQL에서는 미리 인코딩된 해시를 직접 삽입합니다.
--
-- roles 컬럼은 콤마(,) 구분자로 복수 권한을 지정할 수 있습니다.
-- UserInfoUserDetails 생성자에서 split(",")으로 파싱합니다.
--
-- ┌─────────────────────────┬───────────────────────┬────────────┬────────────────────┐
-- │ email                   │ 평문 비밀번호          │ roles      │ 용도               │
-- ├─────────────────────────┼───────────────────────┼────────────┼────────────────────┤
-- │ admin@company.com       │ password              │ ROLE_ADMIN │ 관리자 계정        │
-- │ user1@company.com       │ password              │ ROLE_USER  │ 일반 사용자 1      │
-- │ user2@company.com       │ password              │ ROLE_USER  │ 일반 사용자 2      │
-- │ manager@company.com     │ password              │ ROLE_ADMIN,│ 복합 권한 계정     │
-- │                         │                       │ ROLE_USER  │                    │
-- └─────────────────────────┴───────────────────────┴────────────┴────────────────────┘
--
-- 아래 BCrypt 해시는 모두 평문 'password'를 strength=10으로 인코딩한 값입니다.

INSERT INTO user_info (name, email, password, roles) VALUES
('Admin',
 'admin@company.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'ROLE_ADMIN'),

('User One',
 'user1@company.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'ROLE_USER'),

('User Two',
 'user2@company.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'ROLE_USER'),

('Manager',
 'manager@company.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'ROLE_ADMIN,ROLE_USER');
