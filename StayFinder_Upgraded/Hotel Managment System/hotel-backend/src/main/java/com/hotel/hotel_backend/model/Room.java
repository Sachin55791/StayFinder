package com.hotel.hotel_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hotelId;
    private String roomNumber;
    private String type;
    private String status;
    private double price;
    private String location;
    private String landmark;
    private String facilities;

    public Room() {}

    public Room(Long hotelId, String roomNumber, String type, String status, double price, String location, String landmark, String facilities) {
        this.hotelId = hotelId;
        this.roomNumber = roomNumber;
        this.type = type;
        this.status = status;
        this.price = price;
        this.location = location;
        this.landmark = landmark;
        this.facilities = facilities;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }
}