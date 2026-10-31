package com.hotel.hotel_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.hotel_backend.model.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
