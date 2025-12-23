package com.example.demo.service;

import com.example.demo.entity.FacilityScore;

public interface FacilityScoreService {
    FacilityScore addScore(Long propertyId, FacilityScore score);
    FacilityScore getScoreByProperty(Long propertyId);
}