package com.ikaansh.enrollment.controller;

import com.ikaansh.enrollment.dto.EnrollmentRequest;
import com.ikaansh.enrollment.dto.EnrollmentResponse;
import com.ikaansh.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService service;

    public EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest req) {
        EnrollmentResponse created = service.enroll(req);
        return ResponseEntity.created(URI.create("/api/enrollments/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unenroll(@PathVariable Long id) {
        service.unenroll(id);
        return ResponseEntity.noContent().build();
    }
}
