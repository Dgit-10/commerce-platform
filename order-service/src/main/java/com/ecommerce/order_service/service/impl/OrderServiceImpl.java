package com.ecommerce.order_service.service.impl;

import com.common_packages.common_packages.event.OrderCreatedEvent;
import com.common_packages.common_packages.exception.ResourceNotFoundException;
import com.ecommerce.order_service.client.ProductServiceClient;
import com.ecommerce.order_service.client.UserServiceClient;
import com.ecommerce.order_service.client.dto.ProductResponse;
import com.ecommerce.order_service.dto.CreateOrderRequest;
import com.ecommerce.order_service.dto.OrderItemDto;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.OrderItem;
import com.ecommerce.order_service.entity.OrderStatus;
import com.ecommerce.order_service.kafka.OrderEventProducer;
import com.ecommerce.order_service.repository.OrderRepository;
import com.ecommerce.order_service.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderEventProducer orderEventProducer,
            UserServiceClient userServiceClient,
            ProductServiceClient productServiceClient) {

        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
        this.userServiceClient = userServiceClient;
        this.productServiceClient = productServiceClient;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Calculate total price dynamically
        userServiceClient.getUser(request.getUserId());

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDto itemDto : request.getItems()) {

            ProductResponse product =
                    productServiceClient.getProduct(itemDto.getProductId());

            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product ID: "
                                + itemDto.getProductId());
            }

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            totalAmount = totalAmount.add(itemTotal);

            OrderItem item = new OrderItem(
                    product.getId(),
                    itemDto.getQuantity(),
                    product.getPrice()
            );

            orderItems.add(item);
        }

        Order order = new Order(request.getUserId(), totalAmount);

        request.getItems().forEach(itemDto -> {
            OrderItem item = new OrderItem(itemDto.getProductId(), itemDto.getQuantity(), itemDto.getPrice());
            order.addItem(item);
        });

        Order savedOrder = orderRepository.save(order);

        // Map items for Kafka event
        List<OrderCreatedEvent.OrderItemEventDto> eventItems = savedOrder.getItems().stream()
                .map(i -> new OrderCreatedEvent.OrderItemEventDto(i.getProductId(), i.getQuantity(), i.getPrice()))
                .collect(Collectors.toList());

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getTotalAmount(),
                eventItems,
                LocalDateTime.now()
        );

        // Publish to Kafka
        orderEventProducer.publishOrderCreatedEvent(event);

        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        order.setStatus(status);
        orderRepository.save(order);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(i -> new OrderItemDto(i.getProductId(), i.getQuantity(), i.getPrice()))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                itemDtos,
                order.getCreatedAt()
        );
    }
}