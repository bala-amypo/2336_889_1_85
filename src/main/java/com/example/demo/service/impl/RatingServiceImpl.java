package com.example.demo.service.impl;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.RatingService;
import org.springframework.stereotype.Service;

@Service
public class RatingServiceImpl implements RatingService {

    private final PropertyRepository propertyRepo;
    private final FacilityScoreRepository scoreRepo;
    private final RatingResultRepository ratingRepo;
    private final RatingLogRepository logRepo;

    public RatingServiceImpl(PropertyRepository propertyRepo,
                             FacilityScoreRepository scoreRepo,
                             RatingResultRepository ratingRepo,
                             RatingLogRepository logRepo) {
        this.propertyRepo = propertyRepo;
        this.scoreRepo = scoreRepo;
        this.ratingRepo = ratingRepo;
        this.logRepo = logRepo;
    }

    @Override
    public RatingResult generateRating(Long propertyId) {

        Property property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        FacilityScore fs = scoreRepo.findByProperty(property)
                .orElseThrow(() -> new IllegalArgumentException("Facility score missing"));

        double avg = (fs.getSchoolProximity()
                    + fs.getHospitalProximity()
                    + fs.getTransportAccess()
                    + fs.getSafetyScore()) / 4.0;

        RatingResult result = new RatingResult();
        result.setProperty(property);
        result.setFinalRating(avg);
        result.setRatingCategory(resolveCategory(avg));

        RatingResult saved = ratingRepo.save(result);

        RatingLog log = new RatingLog();
        log.setProperty(property);
        log.setMessage("Rating generated: " + avg);
        logRepo.save(log);

        return saved;
    }

    @Override
    public RatingResult getRating(Long propertyId) {

        Property property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        return ratingRepo.findByProperty(property)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found"));
    }

    private String resolveCategory(double rating) {
        if (rating >= 8) return "EXCELLENT";
        if (rating >= 5) return "GOOD";
        return "POOR";
    }
}
