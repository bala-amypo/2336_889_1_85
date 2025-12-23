package com.example.demo.repository;

import com.example.demo.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    
    @Query("SELECT p FROM Property p WHERE p.city = :city")
    List<Property> findByCityHql(@Param("city") String city);
    
    List<Property> findByCity(String city);
}