package com.ecommerce.user_service.service.impl;

import com.ecommerce.user_service.dto.CreateUserRequest;
import com.ecommerce.user_service.dto.UpdateUserRequest;
import com.ecommerce.user_service.dto.UserResponse;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.mapper.UserMapper;
import com.ecommerce.user_service.repository.UserRepository;
import com.ecommerce.user_service.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        // Business Rule 1: Email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        // Business Rule 2: Optional Phone uniqueness
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        // Business Rule 3: Map DTO to Entity & Set Defaults
        User user = userMapper.toEntity(request);
        user.setUserId(UUID.randomUUID().toString());
        user.setRole(User.Role.CUSTOMER);
        user.setStatus(User.Status.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        // Business Rule 1: User must exist
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Business Rule 2: Update allowed fields
        userMapper.updateEntityFromDto(request, existingUser);

        // Business Rule 3: Update timestamp
        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(UUID userId) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepository.delete(existingUser);
    }
}