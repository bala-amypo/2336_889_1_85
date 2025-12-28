package com.example.demo.repository;

import com.example.demo.entity.Property;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByCity(String city);

    @Query("select p from Property p where p.city = :city")
    List<Property> findByCityHql(String city);
}
