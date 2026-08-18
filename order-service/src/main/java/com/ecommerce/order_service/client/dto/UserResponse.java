package com.ecommerce.order_service.client.dto;

public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String address;

    public UserResponse() {
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }
}