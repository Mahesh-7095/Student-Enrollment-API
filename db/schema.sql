-- PostgreSQL schema for the Student Course Enrollment API
-- Usage:
--   createdb enrollmentdb
--   psql -d enrollmentdb -f db/schema.sql
-- (The app also creates/updates these tables automatically via Hibernate ddl-auto=update.)

CREATE TABLE IF NOT EXISTS students (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    email            VARCHAR(150) NOT NULL,
    enrollment_date  DATE         NOT NULL,
    CONSTRAINT uk_students_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS courses (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    description  VARCHAR(1000),
    credits      INTEGER      NOT NULL CHECK (credits BETWEEN 1 AND 10)
);

CREATE TABLE IF NOT EXISTS enrollments (
    id           BIGSERIAL PRIMARY KEY,
    student_id   BIGINT    NOT NULL REFERENCES students(id),
    course_id    BIGINT    NOT NULL REFERENCES courses(id),
    enrolled_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    -- prevents duplicate enrollment of the same student in the same course
    CONSTRAINT uk_enrollment_student_course UNIQUE (student_id, course_id)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_course ON enrollments(course_id);

-- Optional sample data
INSERT INTO students (name, email, enrollment_date) VALUES
  ('Asha Rao',  'asha@example.com',  CURRENT_DATE),
  ('Ravi Kumar','ravi@example.com',  CURRENT_DATE)
ON CONFLICT DO NOTHING;
INSERT INTO courses (title, description, credits) VALUES
  ('Java Fundamentals', 'Core Java and OOP', 4),
  ('Spring Boot', 'REST APIs with Spring Boot', 3);
