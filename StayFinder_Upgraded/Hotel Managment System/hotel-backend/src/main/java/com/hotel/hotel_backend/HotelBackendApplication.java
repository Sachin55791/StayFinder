package com.hotel.hotel_backend;
import org.springframework.boot.SpringApplication;import org.springframework.boot.autoconfigure.SpringBootApplication;import org.springframework.web.bind.annotation.GetMapping;import org.springframework.web.bind.annotation.RestController;
@SpringBootApplication @RestController public class HotelBackendApplication {public static void main(String[] args){SpringApplication.run(HotelBackendApplication.class,args);}@GetMapping("/test") public String testServer(){return "StayFinder backend is running";}}
