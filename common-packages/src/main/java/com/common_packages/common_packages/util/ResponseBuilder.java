package com.common_packages.common_packages.util;

import com.common_packages.common_packages.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class ResponseBuilder {

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message, HttpStatus status) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
        return new ResponseEntity<>(response, status);
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return success(data, message, HttpStatus.OK);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String message, HttpStatus status) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
        return new ResponseEntity<>(response, status);
    }
}