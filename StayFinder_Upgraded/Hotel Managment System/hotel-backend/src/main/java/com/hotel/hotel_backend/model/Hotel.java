package com.hotel.hotel_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;
    private String name;
    private String city;
    private String location;
    private String landmark;
    private double rating;
    private double startingPrice;
    private String facilities;
    private String imageUrl;
    
    // Status: PENDING, APPROVED, REJECTED
    private String status;

    public Hotel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
