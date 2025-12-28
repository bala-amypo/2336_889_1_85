package com.example.demo.controller;

import com.example.demo.entity.Property;
import com.example.demo.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    // ---------------- CREATE PROPERTY ----------------
    // Used by tests (ADMIN only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Property> addProperty(
            @Valid @RequestBody Property property) {

        Property saved = propertyService.addProperty(property);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ---------------- LIST PROPERTIES ----------------
    @GetMapping
    public Page<Property> listProperties(
            Pageable pageable,
            @RequestParam(required = false) String city) {

        return propertyService.listProperties(pageable, city);
    }

    // ---------------- GET PROPERTY BY ID ----------------
    @GetMapping("/{id}")
    public Property getProperty(@PathVariable Long id) {
        return propertyService.getProperty(id);
    }
}
