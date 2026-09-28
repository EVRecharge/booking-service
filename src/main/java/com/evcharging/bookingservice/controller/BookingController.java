package com.evcharging.bookingservice.controller;

import com.evcharging.bookingservice.dto.BookingDTO;
import com.evcharging.bookingservice.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingDTO bookingDTO) {
        if (!isEntity("USER", bookingDTO.getUserId())) {
            return forbidden("You can only create bookings for yourself");
        }
        try {
            BookingDTO createdBooking = bookingService.createBooking(bookingDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Booking created", "booking", createdBooking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable Long bookingId) {
        try {
            BookingDTO booking = bookingService.getBookingById(bookingId);
            if (!isEntity("USER", booking.getUserId()) && !isEntity("PROVIDER", booking.getProviderId())) {
                return forbidden("You can only view your own bookings");
            }
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUserId(@PathVariable Long userId) {
        if (!isEntity("USER", userId)) {
            return forbidden("You can only view your own bookings");
        }
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getBookingsByProviderId(@PathVariable Long providerId) {
        if (!isEntity("PROVIDER", providerId)) {
            return forbidden("You can only view your own bookings");
        }
        return ResponseEntity.ok(bookingService.getBookingsByProviderId(providerId));
    }

    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long bookingId, @RequestParam String status) {
        try {
            BookingDTO existing = bookingService.getBookingById(bookingId);
            if (!isEntity("PROVIDER", existing.getProviderId())) {
                return forbidden("Only the servicing provider can update a booking's status");
            }
            return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, status));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            BookingDTO existing = bookingService.getBookingById(bookingId);
            if (!isEntity("USER", existing.getUserId()) && !isEntity("PROVIDER", existing.getProviderId())) {
                return forbidden("You can only cancel your own bookings");
            }
            return ResponseEntity.ok(Map.of("message", "Booking cancelled",
                    "booking", bookingService.cancelBooking(bookingId)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long bookingId) {
        try {
            BookingDTO existing = bookingService.getBookingById(bookingId);
            if (!isEntity("USER", existing.getUserId()) && !isEntity("PROVIDER", existing.getProviderId())) {
                return forbidden("You can only delete your own bookings");
            }
            bookingService.deleteBooking(bookingId);
            return ResponseEntity.ok(Map.of("message", "Booking deleted"));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<?> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", message));
    }

    /** True when the caller's JWT identifies them as this exact entityType + id (e.g. "USER", 42). */
    private boolean isEntity(String entityType, Long id) {
        if (id == null) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            return false;
        }
        boolean matchesType = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + entityType));
        return matchesType && id.equals(auth.getPrincipal());
    }
}
