package com.ecommerce.order_service.dto;

public class ServiceResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public ServiceResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}