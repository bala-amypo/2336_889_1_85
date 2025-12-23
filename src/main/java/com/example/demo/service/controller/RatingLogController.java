package com.example.demo.controller;

import com.example.demo.entity.RatingLog;
import com.example.demo.service.RatingLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logs")
public class RatingLogController {

    private final RatingLogService ratingLogService;

    public RatingLogController(RatingLogService ratingLogService) {
        this.ratingLogService = ratingLogService;
    }

    @PostMapping("/{propertyId}")
    public ResponseEntity<RatingLog> addLog(@PathVariable Long propertyId, 
                                           @RequestBody Map<String, String> request) {
        String message = request.get("message");
        RatingLog log = ratingLogService.addLog(propertyId, message);
        return ResponseEntity.ok(log);
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<List<RatingLog>> getLogs(@PathVariable Long propertyId) {
        List<RatingLog> logs = ratingLogService.getLogsByProperty(propertyId);
        return ResponseEntity.ok(logs);
    }
}