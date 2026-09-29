package com.evcharging.bookingservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Minimal projection of user-service's UserDTO -- only the fields booking-service needs to notify a driver. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserContact {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String fullName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        String name = (first + " " + last).trim();
        return name.isEmpty() ? "A driver" : name;
    }
}
