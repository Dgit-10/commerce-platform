package com.ecommerce.user_service.dto;

public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String address;

    public UserResponse(Long id, String email, String fullName, String phoneNumber, String address) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
}