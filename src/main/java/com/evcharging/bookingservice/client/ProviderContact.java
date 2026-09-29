package com.evcharging.bookingservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Minimal projection of provider-service's ProviderDTO -- only the fields booking-service needs to notify a station. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderContact {
    private String companyName;
    private String email;
    private String phoneNumber;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String displayName() {
        return companyName != null && !companyName.isBlank() ? companyName : "Your charging station";
    }
}
