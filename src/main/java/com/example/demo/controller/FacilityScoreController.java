package com.example.demo.controller;

import com.example.demo.entity.FacilityScore;
import com.example.demo.service.FacilityScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scores")
public class FacilityScoreController {

    private final FacilityScoreService facilityScoreService;

    public FacilityScoreController(FacilityScoreService facilityScoreService) {
        this.facilityScoreService = facilityScoreService;
    }

    @PostMapping("/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacilityScore> createScore(@PathVariable Long propertyId, 
                                                    @RequestBody FacilityScore score) {
        FacilityScore savedScore = facilityScoreService.addScore(propertyId, score);
        return ResponseEntity.ok(savedScore);
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<FacilityScore> getScore(@PathVariable Long propertyId) {
        FacilityScore score = facilityScoreService.getScoreByProperty(propertyId);
        return ResponseEntity.ok(score);
    }
}