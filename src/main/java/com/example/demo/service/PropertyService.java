package com.example.demo.service;

import com.example.demo.entity.Property;
import java.util.List;

public interface PropertyService {
    Property addProperty(Property property);
    List<Property> getAllProperties();
    Property findById(Long id);
}