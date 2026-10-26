package com.ikaansh.enrollment.controller;

import com.ikaansh.enrollment.dto.*;
import com.ikaansh.enrollment.service.CourseService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final java.util.Set<String> SORT_FIELDS = java.util.Set.of("id", "title", "credits");

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest req) {
        CourseResponse created = service.create(req);
        return ResponseEntity.created(URI.create("/api/courses/" + created.id())).body(created);
    }

    @GetMapping
    public PageResponse<CourseResponse> list(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "id") String sortBy,
                                             @RequestParam(defaultValue = "asc") String direction) {
        return service.list(Paging.of(page, size, sortBy, direction, SORT_FIELDS));
    }

    @GetMapping("/{id}")
    public CourseResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Bonus: students enrolled in a given course (paginated). */
    @GetMapping("/{id}/students")
    public PageResponse<StudentResponse> students(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return service.studentsInCourse(id, Paging.of(page, size, "id", "asc", java.util.Set.of("id")));
    }
}
