package com.ecommerce.user_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    private String firstName;

    private String lastName;

    private String phoneNumber;
}
