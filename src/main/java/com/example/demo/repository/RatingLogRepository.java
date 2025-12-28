package com.example.demo.repository;

import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RatingLogRepository extends JpaRepository<RatingLog, Long> {
    List<RatingLog> findByProperty(Property property);
}
