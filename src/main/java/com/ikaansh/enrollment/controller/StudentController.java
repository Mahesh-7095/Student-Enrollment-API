package com.ikaansh.enrollment.controller;

import com.ikaansh.enrollment.dto.*;
import com.ikaansh.enrollment.service.StudentService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private static final java.util.Set<String> SORT_FIELDS = java.util.Set.of("id", "name", "email", "enrollmentDate");

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest req) {
        StudentResponse created = service.create(req);
        return ResponseEntity.created(URI.create("/api/students/" + created.id())).body(created);
    }

    @GetMapping
    public PageResponse<StudentResponse> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(defaultValue = "id") String sortBy,
                                              @RequestParam(defaultValue = "asc") String direction) {
        return service.list(Paging.of(page, size, sortBy, direction, SORT_FIELDS));
    }

    @GetMapping("/{id}")
    public StudentResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
