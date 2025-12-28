package com.example.demo.service.impl;

import com.example.demo.entity.Property;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.service.PropertyService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository repository;

    public PropertyServiceImpl(PropertyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Property addProperty(Property property) {
        if (property.getPrice() <= 0) {
            throw new IllegalArgumentException("Invalid price");
        }
        return repository.save(property);
    }

    @Override
    public Page<Property> listProperties(Pageable pageable, String city) {
        if (city != null && !city.isEmpty()) {
            return new PageImpl<>(repository.findByCity(city));
        }
        return repository.findAll(pageable);
    }

    @Override
    public Property getProperty(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));
    }
}
