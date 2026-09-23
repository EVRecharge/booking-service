package com.evcharging.servicebooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
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
