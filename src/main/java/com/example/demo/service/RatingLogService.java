package com.example.demo.service;

import com.example.demo.entity.RatingLog;
import java.util.List;

public interface RatingLogService {
    RatingLog addLog(Long propertyId, String message);
    List<RatingLog> getLogsByProperty(Long propertyId);
}