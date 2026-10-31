package com.hotel.hotel_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.hotel_backend.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByGuestEmail(String guestEmail);

    List<Booking> findByHotelIdInAndBookingStatus(
        List<Long> hotelIds,
        String bookingStatus
    );
}