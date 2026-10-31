package com.hotel.hotel_backend.dto;
import jakarta.validation.constraints.*;
public record AuthRequest(@NotBlank String name,@Email @NotBlank String email,@NotBlank @Size(min=8) String password,String phone,String role){}
