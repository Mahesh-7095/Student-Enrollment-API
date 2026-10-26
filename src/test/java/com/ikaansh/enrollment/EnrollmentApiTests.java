package com.ikaansh.enrollment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // each test rolls back, so tests stay independent
class EnrollmentApiTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private long create(String url, String json) throws Exception {
        String body = mvc.perform(post(url).with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asLong();
    }

    private long newStudent(String email) throws Exception {
        return create("/api/students", "{\"name\":\"Asha\",\"email\":\"" + email + "\"}");
    }

    private long newCourse() throws Exception {
        return create("/api/courses", "{\"title\":\"Spring Boot\",\"description\":\"REST\",\"credits\":3}");
    }

    @Test
    void createStudent_returns201_andDefaultsEnrollmentDate() throws Exception {
        mvc.perform(post("/api/students").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Asha\",\"email\":\"asha@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.enrollmentDate").isNotEmpty());
    }

    @Test
    void createStudent_invalidPayload_returns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/students").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void createStudent_duplicateEmail_returns409() throws Exception {
        newStudent("dup@example.com");
        mvc.perform(post("/api/students").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Other\",\"email\":\"DUP@example.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getStudent_notFound_returns404() throws Exception {
        mvc.perform(get("/api/students/99999").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void enroll_thenDuplicateEnroll_returns409() throws Exception {
        long sid = newStudent("enroll@example.com");
        long cid = newCourse();
        String body = "{\"studentId\":" + sid + ",\"courseId\":" + cid + "}";

        mvc.perform(post("/api/enrollments").with(httpBasic("student", "student123"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/enrollments").with(httpBasic("student", "student123"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void listStudentsInCourse_returnsEnrolledStudents() throws Exception {
        long sid = newStudent("list@example.com");
        long cid = newCourse();
        create("/api/enrollments", "{\"studentId\":" + sid + ",\"courseId\":" + cid + "}");

        mvc.perform(get("/api/courses/" + cid + "/students").with(httpBasic("student", "student123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("list@example.com"));
    }

    @Test
    void studentRole_cannotCreateCourse_returns403() throws Exception {
        mvc.perform(post("/api/courses").with(httpBasic("student", "student123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"X\",\"credits\":3}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void noCredentials_returns401() throws Exception {
        mvc.perform(get("/api/courses")).andExpect(status().isUnauthorized());
    }

    @Test
    void courseList_isPaginated() throws Exception {
        newCourse();
        mvc.perform(get("/api/courses?page=0&size=5").with(httpBasic("student", "student123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.content").isArray());
    }
}
