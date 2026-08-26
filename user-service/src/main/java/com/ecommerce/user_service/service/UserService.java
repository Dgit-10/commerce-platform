package com.ecommerce.user_service.service;

import com.common_packages.common_packages.dto.AuthResponse;
import com.common_packages.common_packages.dto.UserLoginRequest;
import com.ecommerce.user_service.dto.UserRegistrationRequest;
import com.ecommerce.user_service.dto.UserResponse;

public interface UserService {
    UserResponse registerUser(UserRegistrationRequest request);
    AuthResponse loginUser(UserLoginRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
}
