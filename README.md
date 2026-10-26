# Student Course Enrollment REST API

Spring Boot 3 · Spring Data JPA · Bean Validation · Spring Security · H2 (default) / PostgreSQL · JUnit 5 + MockMvc

Manages **students**, **courses** and **enrollments**, with validation, consistent JSON errors,
duplicate-enrollment protection, pagination and role-based access control.

## 1. Requirements
- Java 17+
- Maven 3.8+ (or use your IDE's Maven support)

## 2. Run (zero setup – in-memory H2)
```bash
mvn spring-boot:run
```
API: `http://localhost:8080` · H2 console: `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:mem:enrollmentdb`, user `sa`, empty password). Data resets on restart.

## 3. Run with PostgreSQL
```bash
createdb enrollmentdb
psql -d enrollmentdb -f db/schema.sql          # schema (+ optional sample rows)
export DB_URL=jdbc:postgresql://localhost:5432/enrollmentdb
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```
The schema file is in `db/schema.sql`. (Hibernate `ddl-auto=update` would also create the tables.)
There is no frontend – this submission is backend only.

## 4. Run tests
```bash
mvn test
```

## 5. Users (HTTP Basic)
| user    | password   | role    | can do |
|---------|------------|---------|--------|
| admin   | admin123   | ADMIN   | everything |
| student | student123 | STUDENT | read everything, create/update students, enroll/unenroll |

Course create/update/delete and student delete are **ADMIN only** (students get `403`; no credentials gets `401`).

## 6. Endpoints
| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/students` | `name`, `email` (unique), optional `enrollmentDate` (defaults to today) |
| GET | `/api/students?page=0&size=10&sortBy=name&direction=asc` | paginated; sortBy: id, name, email, enrollmentDate |
| GET / PUT / DELETE | `/api/students/{id}` | delete also removes the student's enrollments |
| POST | `/api/courses` | `title`, `description`, `credits` (1–10) – ADMIN |
| GET | `/api/courses?page=0&size=10&sortBy=title` | paginated; sortBy: id, title, credits |
| GET / PUT / DELETE | `/api/courses/{id}` | PUT/DELETE – ADMIN |
| GET | `/api/courses/{id}/students?page=0&size=10` | students enrolled in a course (bonus) |
| POST | `/api/enrollments` | `{ "studentId": 1, "courseId": 1 }` → 409 if already enrolled |
| DELETE | `/api/enrollments/{id}` | un-enroll |

### Error format (all errors)
```json
{ "timestamp": "2026-10-26T10:00:00Z", "status": 400, "error": "Bad Request",
  "message": "Validation failed", "path": "/api/students",
  "fieldErrors": { "email": "email must be a valid email address" } }
```
Status codes: `400` validation / malformed JSON / bad paging params, `401`, `403`, `404`, `409` (duplicate email or enrollment).

## 7. Sample curl requests
```bash
# create a student
curl -u admin:admin123 -X POST localhost:8080/api/students -H "Content-Type: application/json" \
  -d '{"name":"Asha Rao","email":"asha@example.com"}'

# create a course (admin)
curl -u admin:admin123 -X POST localhost:8080/api/courses -H "Content-Type: application/json" \
  -d '{"title":"Spring Boot","description":"REST APIs","credits":3}'

# enroll student 1 in course 1 (as a student)
curl -u student:student123 -X POST localhost:8080/api/enrollments -H "Content-Type: application/json" \
  -d '{"studentId":1,"courseId":1}'

# duplicate enrollment -> 409
curl -i -u student:student123 -X POST localhost:8080/api/enrollments -H "Content-Type: application/json" \
  -d '{"studentId":1,"courseId":1}'

# students in a course
curl -u student:student123 "localhost:8080/api/courses/1/students"

# paginated lists
curl -u student:student123 "localhost:8080/api/students?page=0&size=5&sortBy=name&direction=desc"
curl -u student:student123 "localhost:8080/api/courses?page=0&size=5"

# update / delete
curl -u admin:admin123 -X PUT localhost:8080/api/students/1 -H "Content-Type: application/json" \
  -d '{"name":"Asha R","email":"asha@example.com"}'
curl -u admin:admin123 -X DELETE localhost:8080/api/students/1

# validation error (400)
curl -i -u admin:admin123 -X POST localhost:8080/api/students -H "Content-Type: application/json" \
  -d '{"name":"","email":"bad"}'

# role check: student creating a course -> 403
curl -i -u student:student123 -X POST localhost:8080/api/courses -H "Content-Type: application/json" \
  -d '{"title":"X","credits":3}'
```

## 8. Design notes
- **Layers:** controller → service → repository. DTO records keep entities out of the API.
- **Duplicate enrollment:** checked in `EnrollmentService` (clean 409 message) *and* enforced by a DB
  unique constraint on `(student_id, course_id)` – the constraint protects against race conditions.
- **Validation:** Jakarta Bean Validation on request DTOs; `GlobalExceptionHandler` maps every failure to one JSON shape.
- **Security:** HTTP Basic with two in-memory users, stateless, CSRF disabled (no browser sessions). Not production-grade;
  a real system would use a user table + JWT/OAuth.
- **Pagination:** custom `PageResponse` for a stable JSON contract; page size capped at 100; sort fields whitelisted.
