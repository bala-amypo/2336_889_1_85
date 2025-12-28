package com.example.demo.service;

import com.example.demo.entity.RatingResult;

public interface RatingService {
    RatingResult generateRating(Long propertyId);
    RatingResult getRating(Long propertyId);
}
