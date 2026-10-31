package com.hotel.hotel_backend.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hotel.hotel_backend.model.Booking;
import com.hotel.hotel_backend.model.Hotel;
import com.hotel.hotel_backend.repository.BookingRepository;
import com.hotel.hotel_backend.repository.HotelRepository;
import com.hotel.hotel_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookings;
    private final UserRepository users;
    private final HotelRepository hotels;

    public BookingController(
            BookingRepository bookings,
            UserRepository users,
            HotelRepository hotels) {
        this.bookings = bookings;
        this.users = users;
        this.hotels = hotels;
    }

    // USER: Create booking request
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody Booking b,
            Authentication auth) {

        if (auth == null) {
            return ResponseEntity.status(401).body(
                Map.of("message", "Please login first")
            );
        }

        if (b.getHotelId() == null) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Hotel ID is required")
            );
        }

        Hotel hotel = hotels.findById(b.getHotelId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Hotel not found"
            ));

        if (!"APPROVED".equalsIgnoreCase(hotel.getStatus())) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "This hotel is not available for booking")
            );
        }

        if (b.getCheckIn() == null || b.getCheckOut() == null) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Check-in and check-out are required")
            );
        }

        try {
            LocalDate in = LocalDate.parse(b.getCheckIn());
            LocalDate out = LocalDate.parse(b.getCheckOut());

            if (!out.isAfter(in) || in.isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "Please select valid future dates")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Dates must use YYYY-MM-DD format")
            );
        }

        var user = users.findByEmail(auth.getName()).orElseThrow();

        b.setUserId(user.getId());
        b.setGuestEmail(user.getEmail());
        b.setLocation(hotel.getName() + " - " + hotel.getLocation());
        b.setBookingStatus("PENDING");
        b.setPaymentStatus("PENDING");

        return ResponseEntity.status(201).body(bookings.save(b));
    }

    // USER: See own bookings
    @GetMapping("/mine")
    public List<Booking> mine(Authentication auth) {
        return bookings.findByGuestEmail(auth.getName());
    }

    // OWNER: See pending requests for own hotels
    @GetMapping("/owner/pending")
    public ResponseEntity<?> ownerPending(Authentication auth) {

        var owner = users.findByEmail(auth.getName()).orElseThrow();

        if (!"OWNER".equalsIgnoreCase(owner.getRole())) {
            return ResponseEntity.status(403).body(
                Map.of("message", "Only hotel owners can view these bookings")
            );
        }

        List<Hotel> ownerHotels = hotels.findByOwnerId(owner.getId());

        List<Long> hotelIds = new ArrayList<>();

        for (Hotel hotel : ownerHotels) {
            hotelIds.add(hotel.getId());
        }

        if (hotelIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        return ResponseEntity.ok(
            bookings.findByHotelIdInAndBookingStatus(hotelIds, "PENDING")
        );
    }

    // OWNER: Accept or reject a booking
    @PutMapping("/owner/{id}/decision")
    public ResponseEntity<?> decideBooking(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication auth) {

        var owner = users.findByEmail(auth.getName()).orElseThrow();

        if (!"OWNER".equalsIgnoreCase(owner.getRole())) {
            return ResponseEntity.status(403).body(
                Map.of("message", "Only hotel owners can update bookings")
            );
        }

        if (!status.equalsIgnoreCase("APPROVED")
                && !status.equalsIgnoreCase("REJECTED")) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Status must be APPROVED or REJECTED")
            );
        }

        Booking booking = bookings.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Booking not found"
            ));

        Hotel hotel = hotels.findById(booking.getHotelId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Hotel not found"
            ));

        if (!Objects.equals(hotel.getOwnerId(), owner.getId())) {
            return ResponseEntity.status(403).body(
                Map.of("message", "You cannot manage another owner's booking")
            );
        }

        if (!"PENDING".equalsIgnoreCase(booking.getBookingStatus())) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "This booking has already been processed")
            );
        }

        booking.setBookingStatus(status.toUpperCase());

        return ResponseEntity.ok(bookings.save(booking));
    }

    // USER: Cancel own booking
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable Long id,
            Authentication auth) {

        Booking b = bookings.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND
            ));

        if (!auth.getName().equalsIgnoreCase(b.getGuestEmail())) {
            return ResponseEntity.status(403).body(
                Map.of("message", "Not your booking")
            );
        }

        if (!"PENDING".equalsIgnoreCase(b.getBookingStatus())) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Only pending bookings can be cancelled")
            );
        }

        b.setBookingStatus("CANCELLED");

        return ResponseEntity.ok(bookings.save(b));
    }
}