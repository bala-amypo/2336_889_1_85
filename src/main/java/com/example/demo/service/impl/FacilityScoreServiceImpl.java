package com.example.demo.service.impl;

import com.example.demo.entity.FacilityScore;
import com.example.demo.entity.Property;
import com.example.demo.repository.FacilityScoreRepository;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.service.FacilityScoreService;
import org.springframework.stereotype.Service;

@Service
public class FacilityScoreServiceImpl implements FacilityScoreService {

    private final FacilityScoreRepository scoreRepo;
    private final PropertyRepository propertyRepo;

    public FacilityScoreServiceImpl(FacilityScoreRepository scoreRepo,
                                    PropertyRepository propertyRepo) {
        this.scoreRepo = scoreRepo;
        this.propertyRepo = propertyRepo;
    }

    @Override
    public FacilityScore createScore(Long propertyId, FacilityScore score) {

        Property property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        // ❌ Only one score allowed
        if (scoreRepo.findByProperty(property).isPresent()) {
            throw new IllegalArgumentException("Facility score already exists");
        }

        score.setProperty(property);
        return scoreRepo.save(score);
    }

    @Override
    public FacilityScore getScore(Long propertyId) {

        Property property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        return scoreRepo.findByProperty(property)
                .orElseThrow(() -> new IllegalArgumentException("Score not found"));
    }
}
