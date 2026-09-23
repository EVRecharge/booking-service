package com.evcharging.servicebooking.service;

import com.evcharging.servicebooking.dto.BookingDTO;
import com.evcharging.servicebooking.entity.Booking;
import com.evcharging.servicebooking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    public BookingDTO createBooking(BookingDTO bookingDTO) {
        Booking booking = new Booking();
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

        Booking savedBooking = bookingRepository.save(booking);
        return convertToDTO(savedBooking);
    }

    public BookingDTO getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return convertToDTO(booking);
    }

    public List<BookingDTO> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<BookingDTO> getBookingsByProviderId(Long providerId) {
        return bookingRepository.findByProviderId(providerId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public BookingDTO updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(status);
        return convertToDTO(bookingRepository.save(booking));
    }

    public BookingDTO cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("CANCELLED");
        return convertToDTO(bookingRepository.save(booking));
    }

    public void deleteBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
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
