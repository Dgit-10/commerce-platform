package com.ecommerce.user_service.service.impl;


import com.common_packages.common_packages.event.UserRegisteredEvent;
import com.common_packages.common_packages.exception.ResourceNotFoundException;
import com.ecommerce.user_service.kafka.UserEventProducer;
import com.ecommerce.user_service.dto.UserRegistrationRequest;
import com.ecommerce.user_service.dto.UserResponse;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.repository.UserRepository;
import com.ecommerce.user_service.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    public UserServiceImpl(UserRepository userRepository, UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
    }

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        // In a production app, pass password through BCryptPasswordEncoder
        String dummyHashedPassword = "hashed_" + request.getPassword();

        User user = new User(
                request.getEmail(),
                dummyHashedPassword,
                request.getFullName(),
                request.getPhoneNumber(),
                request.getAddress()
        );

        User savedUser = userRepository.save(user);

        // Produce Kafka event asynchronously after persistence
        UserRegisteredEvent event = new UserRegisteredEvent(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                LocalDateTime.now()
        );
        userEventProducer.publishUserRegisteredEvent(event);

        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getAddress()
        );
    }
}