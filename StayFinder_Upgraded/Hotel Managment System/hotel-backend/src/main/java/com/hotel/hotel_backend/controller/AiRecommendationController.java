package com.hotel.hotel_backend.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.hotel_backend.model.Hotel;
import com.hotel.hotel_backend.repository.HotelRepository;
import com.hotel.hotel_backend.service.AiService;

@RestController
@RequestMapping("/api/ai")
public class AiRecommendationController {

    private final HotelRepository hotels;
    private final AiService ai;

    public AiRecommendationController(
            HotelRepository hotels,
            AiService ai) {

        this.hotels = hotels;
        this.ai = ai;
    }

    @PostMapping("/recommend")
    public Map<String, Object> recommend(
            @RequestBody Map<String, Object> request) {

        String city = String.valueOf(
                request.getOrDefault("city", "")
        );

        double budget;

        try {
            budget = Double.parseDouble(
                    String.valueOf(
                            request.getOrDefault("maxBudget", "10000")
                    )
            );
        } catch (Exception e) {
            budget = 10000;
        }

        final double budgetLimit = budget;

        List<Hotel> results = hotels
                .findByCityContainingIgnoreCaseAndStatus(
                        city,
                        "APPROVED"
                )
                .stream()
                .filter(hotel -> hotel.getStartingPrice() <= budgetLimit)
                .toList();

        String prompt = String.valueOf(
                request.getOrDefault("prompt", "Recommend hotels")
        );

        String message;

        if (results.isEmpty()) {
            message = "Our database has no matching approved hotel "
                    + "for those filters. Try another city or budget.";
        } else {
            message = ai.summarize(prompt, results);
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message", message);
        response.put("hotels", results);
        response.put("aiEnabled", ai.isEnabled());

        return response;
    }

    /*
     * Separate endpoint to avoid conflict with AiController.
     * Endpoint: POST /api/ai/hotel-chat
     */
    @PostMapping("/hotel-chat")
    public Map<String, String> chat(
            @RequestBody Map<String, String> request) {

        String prompt = request.getOrDefault("prompt", "").trim();

        String reply;

        if (prompt.isBlank()) {
            reply = "Please tell me your destination and approximate budget.";
        } else {
            reply = "Please include a city and budget so I can shortlist "
                    + "verified properties from our hotel database.";
        }

        return Map.of("reply", reply);
    }
}