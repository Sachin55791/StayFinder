package com.hotel.hotel_backend.controller;

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
public class AiController {

    private final HotelRepository hotels;
    private final AiService ai;

    public AiController(HotelRepository hotels, AiService ai) {
        this.hotels = hotels;
        this.ai = ai;
    }

    @PostMapping("/chat")
    public Map<String, Object> chatWithAi(@RequestBody Map<String, String> request) {
        String prompt = request.getOrDefault("prompt", "").trim();
        if (prompt.isBlank()) {
            return Map.of("reply", "Please enter a destination, budget, or hotel question.",
                          "aiEnabled", ai.isEnabled());
        }

        List<Hotel> approvedHotels = hotels.findByStatus("APPROVED");
        String reply = approvedHotels.isEmpty()
            ? "There are currently no approved hotels in StayFinder's database. Please check again later."
            : ai.summarize(prompt, approvedHotels);

        return Map.of("reply", reply, "aiEnabled", ai.isEnabled());
    }
}
