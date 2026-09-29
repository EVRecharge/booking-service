package com.evcharging.bookingservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Looks up contact details (email/phone) for the user or provider on either side of a booking, so
 * booking-service can send them a notification. Calls out to user-service/provider-service through
 * the public API (the same path the frontend uses), authenticated with a short-lived internal
 * service token -- see {@link InternalServiceTokenProvider}.
 */
@Component
public class DirectoryClient {

    private static final Logger log = LoggerFactory.getLogger(DirectoryClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InternalServiceTokenProvider tokenProvider;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    public UserContact findUser(Long userId) {
        try {
            var response = restTemplate.exchange(
                    publicBaseUrl + "/api/users/" + userId, HttpMethod.GET, authorizedRequest(), UserContact.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("Could not look up user {} for a booking notification: {}", userId, e.getMessage());
            return null;
        }
    }

    public ProviderContact findProvider(Long providerId) {
        try {
            var response = restTemplate.exchange(
                    publicBaseUrl + "/api/providers/" + providerId, HttpMethod.GET, authorizedRequest(),
                    ProviderContact.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("Could not look up provider {} for a booking notification: {}", providerId, e.getMessage());
            return null;
        }
    }

    private HttpEntity<Void> authorizedRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenProvider.mintServiceToken());
        return new HttpEntity<>(headers);
    }
}
