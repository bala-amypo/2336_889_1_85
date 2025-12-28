package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
public class FacilityScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "property_id", unique = true)
    private Property property;

    @Min(1) @Max(10)
    private int schoolProximity;

    @Min(1) @Max(10)
    private int hospitalProximity;

    @Min(1) @Max(10)
    private int transportAccess;

    @Min(1) @Max(10)
    private int safetyScore;

    // getters & setters
    public Long getId() { return id; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public int getSchoolProximity() { return schoolProximity; }
    public void setSchoolProximity(int schoolProximity) { this.schoolProximity = schoolProximity; }

    public int getHospitalProximity() { return hospitalProximity; }
    public void setHospitalProximity(int hospitalProximity) { this.hospitalProximity = hospitalProximity; }

    public int getTransportAccess() { return transportAccess; }
    public void setTransportAccess(int transportAccess) { this.transportAccess = transportAccess; }

    public int getSafetyScore() { return safetyScore; }
    public void setSafetyScore(int safetyScore) { this.safetyScore = safetyScore; }
}
