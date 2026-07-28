package com.ecommerce.api_gateway.repository;

import com.ecommerce.api_gateway.entity.ApiGateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiGatewayRepository extends JpaRepository<ApiGateway, Long> {
}
