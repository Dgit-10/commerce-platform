package com.ecommerce.user_service.mapper;

import com.ecommerce.user_service.dto.CreateUserRequest;
import com.ecommerce.user_service.dto.UpdateUserRequest;
import com.ecommerce.user_service.dto.UserResponse;
import com.ecommerce.user_service.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }

    public void updateEntityFromDto(UpdateUserRequest request, User user) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }
}
