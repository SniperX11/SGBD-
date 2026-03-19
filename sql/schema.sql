-- ============================================================
-- SGBD Lab 1 - Schema and sample data
-- Compatible with PostgreSQL
-- ============================================================

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS employee_projects CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS departments CASCADE;

-- ============================================================
-- Parent table: departments  (1-N side)
-- ============================================================
CREATE TABLE departments (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    location    VARCHAR(100) NOT NULL,
    budget      NUMERIC(15, 2) DEFAULT 0.00
);

-- ============================================================
-- Child table: employees  (N side of 1-N with departments)
-- ============================================================
CREATE TABLE employees (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    salary          NUMERIC(10, 2) NOT NULL CHECK (salary > 0),
    hire_date       DATE NOT NULL DEFAULT CURRENT_DATE,
    department_id   INT NOT NULL REFERENCES departments(id) ON DELETE CASCADE
);

-- ============================================================
-- M-N relationship: employees <-> projects
-- ============================================================
CREATE TABLE projects (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    deadline    DATE
);

CREATE TABLE employee_projects (
    employee_id INT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    project_id  INT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    role        VARCHAR(100),
    PRIMARY KEY (employee_id, project_id)
);

-- ============================================================
-- Sample data
-- ============================================================

-- Departments (parents)
INSERT INTO departments (name, location, budget) VALUES
    ('Engineering',    'Cluj-Napoca',  500000.00),
    ('Marketing',      'Bucharest',    200000.00),
    ('Human Resources','Cluj-Napoca',  150000.00),
    ('Finance',        'Timisoara',    300000.00);

-- Employees (children)
INSERT INTO employees (name, email, salary, hire_date, department_id) VALUES
    ('Alice Pop',     'alice.pop@company.ro',     6000.00, '2020-03-15', 1),
    ('Bogdan Ionescu','bogdan.ionescu@company.ro', 5500.00, '2019-07-01', 1),
    ('Carmen Dinu',   'carmen.dinu@company.ro',   5800.00, '2021-01-20', 1),
    ('Dan Mihai',     'dan.mihai@company.ro',      4500.00, '2022-06-10', 2),
    ('Elena Stan',    'elena.stan@company.ro',     4800.00, '2021-11-05', 2),
    ('Florin Olaru',  'florin.olaru@company.ro',   5200.00, '2020-08-22', 3),
    ('Gina Popa',     'gina.popa@company.ro',      4700.00, '2023-02-14', 3),
    ('Horia Lungu',   'horia.lungu@company.ro',    6500.00, '2018-05-30', 4),
    ('Ioana Mocanu',  'ioana.mocanu@company.ro',   6200.00, '2019-09-17', 4),
    ('Ion Vasile',    'ion.vasile@company.ro',     5900.00, '2020-12-03', 1);

-- Projects
INSERT INTO projects (name, deadline) VALUES
    ('ERP Migration',   '2025-12-31'),
    ('Brand Refresh',   '2025-06-30'),
    ('HR Portal',       '2024-09-01');

-- Employee-Project assignments (M-N)
INSERT INTO employee_projects (employee_id, project_id, role) VALUES
    (1, 1, 'Lead Developer'),
    (2, 1, 'Backend Developer'),
    (3, 1, 'QA Engineer'),
    (4, 2, 'Campaign Manager'),
    (5, 2, 'Content Creator'),
    (6, 3, 'HR Analyst'),
    (7, 3, 'System Tester'),
    (8, 1, 'Financial Advisor'),
    (1, 3, 'Consultant');
