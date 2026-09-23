// ============= BOOKING SERVICE APPLICATION =============
package com.evcharging.servicebooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceBookingApplication.class, args);
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

// ============= BOOKING ENTITY =============
package com.evcharging.servicebooking.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_bookings", indexes = {
    @Index(name = "idx_bookings_user_id", columnList = "user_id"),
    @Index(name = "idx_bookings_provider_id", columnList = "provider_id"),
    @Index(name = "idx_bookings_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column
    private Integer durationInHours;

    @Column(nullable = false)
    private String status;

    @Column
    private Double totalCost;

    @Column
    private String notes;

    @Column
    private String address;

    @Column
    private String city;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

// ============= BOOKING REPOSITORY =============
package com.evcharging.servicebooking.repository;

import com.evcharging.servicebooking.entity.ServiceBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, Long> {
    List<ServiceBooking> findByUserId(Long userId);
    List<ServiceBooking> findByProviderId(Long providerId);
    List<ServiceBooking> findByStatus(String status);
    List<ServiceBooking> findByUserIdAndStatus(Long userId, String status);
    Optional<ServiceBooking> findByBookingId(Long bookingId);
}

// ============= BOOKING DTO =============
package com.evcharging.servicebooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBookingDTO {
    private Long bookingId;
    @NotNull
    private Long userId;
    @NotNull
    private Long providerId;
    @NotNull
    private LocalDateTime startTime;
    @NotNull
    private LocalDateTime endTime;
    private Integer durationInHours;
    private String status;
    private Double totalCost;
    private String notes;
    private String address;
    private String city;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// ============= BOOKING SERVICE =============
package com.evcharging.servicebooking.service;

import com.evcharging.servicebooking.dto.ServiceBookingDTO;
import com.evcharging.servicebooking.entity.ServiceBooking;
import com.evcharging.servicebooking.repository.ServiceBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceBookingService {

    @Autowired
    private ServiceBookingRepository bookingRepository;

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    public ServiceBookingDTO createBooking(ServiceBookingDTO bookingDTO) {
        ServiceBooking booking = new ServiceBooking();
        booking.setUserId(bookingDTO.getUserId());
        booking.setProviderId(bookingDTO.getProviderId());
        booking.setBookingDate(LocalDateTime.now());
        booking.setStartTime(bookingDTO.getStartTime());
        booking.setEndTime(bookingDTO.getEndTime());
        booking.setNotes(bookingDTO.getNotes());
        booking.setAddress(bookingDTO.getAddress());
        booking.setCity(bookingDTO.getCity());

        long hours = ChronoUnit.HOURS.between(bookingDTO.getStartTime(), bookingDTO.getEndTime());
        booking.setDurationInHours((int) hours);
        booking.setTotalCost(50.0 * hours);
        booking.setStatus("PENDING");

        ServiceBooking savedBooking = bookingRepository.save(booking);
        return convertToDTO(savedBooking);
    }

    public ServiceBookingDTO getBookingById(Long bookingId) {
        ServiceBooking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return convertToDTO(booking);
    }

    public List<ServiceBookingDTO> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ServiceBookingDTO> getBookingsByProviderId(Long providerId) {
        return bookingRepository.findByProviderId(providerId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ServiceBookingDTO updateBookingStatus(Long bookingId, String status) {
        ServiceBooking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(status);
        return convertToDTO(bookingRepository.save(booking));
    }

    public ServiceBookingDTO cancelBooking(Long bookingId) {
        ServiceBooking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("CANCELLED");
        return convertToDTO(bookingRepository.save(booking));
    }

    public void deleteBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    private ServiceBookingDTO convertToDTO(ServiceBooking booking) {
        ServiceBookingDTO dto = new ServiceBookingDTO();
        dto.setBookingId(booking.getBookingId());
        dto.setUserId(booking.getUserId());
        dto.setProviderId(booking.getProviderId());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setDurationInHours(booking.getDurationInHours());
        dto.setStatus(booking.getStatus());
        dto.setTotalCost(booking.getTotalCost());
        dto.setNotes(booking.getNotes());
        dto.setAddress(booking.getAddress());
        dto.setCity(booking.getCity());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        return dto;
    }
}

// ============= BOOKING CONTROLLER =============
package com.evcharging.servicebooking.controller;

import com.evcharging.servicebooking.dto.ServiceBookingDTO;
import com.evcharging.servicebooking.service.ServiceBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ServiceBookingController {

    @Autowired
    private ServiceBookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody ServiceBookingDTO bookingDTO) {
        try {
            ServiceBookingDTO createdBooking = bookingService.createBooking(bookingDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Booking created", "booking", createdBooking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable Long bookingId) {
        try {
            return ResponseEntity.ok(bookingService.getBookingById(bookingId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getBookingsByProviderId(@PathVariable Long providerId) {
        return ResponseEntity.ok(bookingService.getBookingsByProviderId(providerId));
    }

    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long bookingId, @RequestParam String status) {
        try {
            return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, status));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            return ResponseEntity.ok(Map.of("message", "Booking cancelled", 
                    "booking", bookingService.cancelBooking(bookingId)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long bookingId) {
        try {
            bookingService.deleteBooking(bookingId);
            return ResponseEntity.ok(Map.of("message", "Booking deleted"));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
