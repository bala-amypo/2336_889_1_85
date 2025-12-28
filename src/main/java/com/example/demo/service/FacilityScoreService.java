package com.example.demo.service;

import com.example.demo.entity.FacilityScore;

public interface FacilityScoreService {
    FacilityScore createScore(Long propertyId, FacilityScore score);
    FacilityScore getScore(Long propertyId);
}
