package com.hotel.hotel_backend.controller;

import com.hotel.hotel_backend.model.Room;
import com.hotel.hotel_backend.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    // Naya room database mein save karne ke liye
    @PostMapping("/add")
    public Room addRoom(@RequestBody Room room) {
        return roomRepository.save(room);
    }

    // Database se saare rooms fetch karne ke liye
    @GetMapping("/all")
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // Database se specific room delete karne ke liye
    @DeleteMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id) {
        roomRepository.deleteById(id);
        return "Room deleted successfully!";
    }
}