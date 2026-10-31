
package com.hotel.hotel_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hotel.hotel_backend.model.User;
import com.hotel.hotel_backend.repository.UserRepository;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner createInitialAdmin(
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password,
            @Value("${app.admin.name:StayFinder Admin}") String name) {

        return args -> {

            if (email == null || email.isBlank()
                    || password == null || password.length() < 8) {
                System.out.println("Admin credentials missing or password too short.");
                return;
            }

            String normalizedEmail = email.trim().toLowerCase();

            var existingUser = users.findByEmail(normalizedEmail);

            if (existingUser.isEmpty()) {
                User admin = new User();
                admin.setName(name);
                admin.setEmail(normalizedEmail);
                admin.setPassword(encoder.encode(password));
                admin.setRole("ADMIN");

                users.save(admin);

                System.out.println("Initial ADMIN account created successfully.");

            } else {
                System.out.println("Admin email already exists. Existing account unchanged.");
            }
        };
    }
}
