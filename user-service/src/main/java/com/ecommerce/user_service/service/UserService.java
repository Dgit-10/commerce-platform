package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.CreateUserRequest;
import com.ecommerce.user_service.dto.UpdateUserRequest;
import com.ecommerce.user_service.dto.UserResponse;
import com.ecommerce.user_service.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    //Define all the methods which must be implemented
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(UUID userId, UpdateUserRequest userResponse);
    UserResponse getUser(UUID userId);
    List<UserResponse> getAllUsers();
    void deleteUser(UUID userId);
}
