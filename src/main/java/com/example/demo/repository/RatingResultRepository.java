package com.example.demo.repository;

import com.example.demo.entity.RatingResult;
import com.example.demo.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RatingResultRepository extends JpaRepository<RatingResult, Long> {
    Optional<RatingResult> findByProperty(Property property);
}