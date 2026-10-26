package com.ikaansh.enrollment.controller;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Small helper so both list endpoints validate and build paging the same way. */
final class Paging {
    private static final int MAX_SIZE = 100;

    private Paging() {}

    static Pageable of(int page, int size, String sortBy, String direction, Set<String> allowedSortFields) {
        if (page < 0 || size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("page must be >= 0 and size must be between 1 and " + MAX_SIZE);
        }
        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException("sortBy must be one of " + allowedSortFields);
        }
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(dir, sortBy));
    }
}
