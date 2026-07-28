package com.ecommerce.api_gateway.service;

import org.springframework.stereotype.Service;

@Service
public class ApiGatewayService {

    public String healthCheck() {
        return "API Gateway is running";
    }
}
