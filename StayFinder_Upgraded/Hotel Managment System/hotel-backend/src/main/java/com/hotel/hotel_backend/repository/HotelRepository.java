package com.hotel.hotel_backend.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hotel.hotel_backend.model.Hotel;
public interface HotelRepository extends JpaRepository<Hotel,Long>{
 List<Hotel> findByStatus(String status);
 List<Hotel> findByOwnerId(Long ownerId);
 List<Hotel> findByCityContainingIgnoreCaseAndStatus(String city,String status);
 List<Hotel> findByNameContainingIgnoreCaseAndStatusOrCityContainingIgnoreCaseAndStatus(String name,String status1,String city,String status2);
}
