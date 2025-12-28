package com.example.demo.controller;

import com.example.demo.entity.FacilityScore;
import com.example.demo.service.FacilityScoreService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scores")
public class FacilityScoreController {

    private final FacilityScoreService service;

    public FacilityScoreController(FacilityScoreService service) {
        this.service = service;
    }

    @PostMapping("/{propertyId}")
    public ResponseEntity<FacilityScore> create(
            @PathVariable Long propertyId,
            @RequestBody FacilityScore score) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createScore(propertyId, score));
    }

    @GetMapping("/{propertyId}")
    public FacilityScore get(@PathVariable Long propertyId) {
        return service.getScore(propertyId);
    }
}
