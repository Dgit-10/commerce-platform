package com.ecommerce.api_gateway.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "api_gateway")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiGateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;

    private String endpoint;
}
