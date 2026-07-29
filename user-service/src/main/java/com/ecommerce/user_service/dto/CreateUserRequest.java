package com.ecommerce.user_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String phoneNumber;
}
