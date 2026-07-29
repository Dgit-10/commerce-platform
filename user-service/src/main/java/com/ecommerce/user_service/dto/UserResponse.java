package com.ecommerce.user_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String role;

    private String status;
}
