package com.hotel.hotel_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.hotel_backend.model.Feedback;
import com.hotel.hotel_backend.repository.FeedbackRepository;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @PostMapping("/add")
    public Feedback addFeedback(@RequestBody Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    @GetMapping("/all")
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @DeleteMapping("/delete/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        feedbackRepository.deleteById(id);
        return "Feedback deleted successfully";
    }
}