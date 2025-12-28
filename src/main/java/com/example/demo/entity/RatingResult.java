package com.example.demo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
public class RatingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Property property;

    private Double finalRating;
    private String ratingCategory;

    @CreationTimestamp
    private LocalDateTime ratedAt;

    // getters & setters
    public Long getId() { return id; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public Double getFinalRating() { return finalRating; }
    public void setFinalRating(Double finalRating) { this.finalRating = finalRating; }

    public String getRatingCategory() { return ratingCategory; }
    public void setRatingCategory(String ratingCategory) { this.ratingCategory = ratingCategory; }

    public LocalDateTime getRatedAt() { return ratedAt; }
}
