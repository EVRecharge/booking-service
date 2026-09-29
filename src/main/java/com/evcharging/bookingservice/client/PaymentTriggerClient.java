package com.evcharging.bookingservice.client;

import com.evcharging.bookingservice.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Tells payment-service to charge the driver's saved primary payment method the moment a provider
 * confirms a booking -- the actual point of intent for this product, not booking creation. Fires
 * with the same internal service token used for the directory lookups, since there's no live
 * request from the driver to forward at this point (the provider is who's calling
 * updateBookingStatus). Best-effort: if the driver has no saved method yet or the charge fails,
 * the booking is still confirmed -- they can pay manually from My Bookings instead.
 */
@Component
public class PaymentTriggerClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentTriggerClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InternalServiceTokenProvider tokenProvider;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    @Async
    public void chargeOnConfirmation(Booking booking) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokenProvider.mintServiceToken());
            Map<String, Object> body = Map.of(
                    "bookingId", booking.getBookingId(),
                    "userId", booking.getUserId(),
                    "providerId", booking.getProviderId(),
                    "amountRupees", booking.getTotalCost());
            restTemplate.postForEntity(
                    publicBaseUrl + "/api/payments/internal/bookings/" + booking.getBookingId() + "/charge",
                    new HttpEntity<>(body, headers), Void.class);
        } catch (Exception e) {
            log.warn("Could not auto-charge booking {} on confirmation: {}", booking.getBookingId(), e.getMessage());
        }
    }
}
