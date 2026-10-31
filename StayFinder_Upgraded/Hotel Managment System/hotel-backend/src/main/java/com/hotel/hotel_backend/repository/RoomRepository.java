package com.hotel.hotel_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hotel.hotel_backend.model.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}
