package com.ecommerce.user_service.controller;

import com.common_packages.common_packages.dto.ApiResponse;
import com.common_packages.common_packages.dto.AuthResponse;
import com.common_packages.common_packages.dto.UserLoginRequest;
import com.ecommerce.user_service.dto.UserRegistrationRequest;
import com.ecommerce.user_service.dto.UserResponse;
import com.ecommerce.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@Valid @RequestBody UserLoginRequest request) {
        AuthResponse response = userService.loginUser(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            Authentication authentication) {
        String userIdStr = (userIdHeader != null && !userIdHeader.isBlank())
                ? userIdHeader
                : (authentication != null ? authentication.getName() : null);

        if (userIdStr == null) {
            throw new IllegalArgumentException("User context not found in request");
        }

        Long userId = Long.parseLong(userIdStr);
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("Current user profile fetched successfully", response));
    }
}
