-- 부서 추가 (기존 3개 → 총 8개)
insert into departments (department_name, department_description) values
('IT',          'manages information technology infrastructure and software development'),
('Finance',     'handles budgeting, accounting, and financial reporting'),
('Operations',  'oversees day-to-day business processes and efficiency'),
('Legal',       'provides legal counsel and ensures regulatory compliance'),
('R&D',         'researches and develops new products and technologies');

-- 직원 추가 (기존 3명 → 총 23명)
-- department_id는 부서명 서브쿼리로 참조 (auto_increment 값에 의존하지 않음)
insert into employees (email_id, first_name, last_name, department_id)
select 'michael@company.com',  'Michael',  'Davis',     id from departments where department_name = 'HR';
insert into employees (email_id, first_name, last_name, department_id)
select 'jessica@company.com',  'Jessica',  'Wilson',    id from departments where department_name = 'Marketing';
insert into employees (email_id, first_name, last_name, department_id)
select 'daniel@company.com',   'Daniel',   'Martinez',  id from departments where department_name = 'Sales';
insert into employees (email_id, first_name, last_name, department_id)
select 'laura@company.com',    'Laura',    'Anderson',  id from departments where department_name = 'IT';
insert into employees (email_id, first_name, last_name, department_id)
select 'james@company.com',    'James',    'Taylor',    id from departments where department_name = 'Finance';
insert into employees (email_id, first_name, last_name, department_id)
select 'olivia@company.com',   'Olivia',   'Thomas',    id from departments where department_name = 'Operations';
insert into employees (email_id, first_name, last_name, department_id)
select 'william@company.com',  'William',  'Jackson',   id from departments where department_name = 'Legal';
insert into employees (email_id, first_name, last_name, department_id)
select 'sophia@company.com',   'Sophia',   'White',     id from departments where department_name = 'R&D';
insert into employees (email_id, first_name, last_name, department_id)
select 'ethan@company.com',    'Ethan',    'Harris',    id from departments where department_name = 'HR';
insert into employees (email_id, first_name, last_name, department_id)
select 'emma@company.com',     'Emma',     'Martin',    id from departments where department_name = 'Marketing';
insert into employees (email_id, first_name, last_name, department_id)
select 'noah@company.com',     'Noah',     'Garcia',    id from departments where department_name = 'Sales';
insert into employees (email_id, first_name, last_name, department_id)
select 'ava@company.com',      'Ava',      'Lee',       id from departments where department_name = 'IT';
insert into employees (email_id, first_name, last_name, department_id)
select 'liam@company.com',     'Liam',     'Clark',     id from departments where department_name = 'Finance';
insert into employees (email_id, first_name, last_name, department_id)
select 'mia@company.com',      'Mia',      'Lewis',     id from departments where department_name = 'Operations';
insert into employees (email_id, first_name, last_name, department_id)
select 'mason@company.com',    'Mason',    'Walker',    id from departments where department_name = 'Legal';
insert into employees (email_id, first_name, last_name, department_id)
select 'isabella@company.com', 'Isabella', 'Hall',      id from departments where department_name = 'R&D';
insert into employees (email_id, first_name, last_name, department_id)
select 'logan@company.com',    'Logan',    'Allen',     id from departments where department_name = 'HR';
insert into employees (email_id, first_name, last_name, department_id)
select 'charlotte@company.com','Charlotte','Young',     id from departments where department_name = 'Marketing';
insert into employees (email_id, first_name, last_name, department_id)
select 'lucas@company.com',    'Lucas',    'King',      id from departments where department_name = 'Sales';
insert into employees (email_id, first_name, last_name, department_id)
select 'amelia@company.com',   'Amelia',   'Wright',    id from departments where department_name = 'IT';
