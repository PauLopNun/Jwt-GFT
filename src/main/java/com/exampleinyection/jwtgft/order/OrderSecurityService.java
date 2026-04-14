package com.exampleinyection.jwtgft.order;

import org.springframework.stereotype.Service;

@Service("orderSecurityService")
public class OrderSecurityService {

    private final OrderRepository orderRepository;

    public OrderSecurityService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public boolean isOwner(Long orderId, String email) {
        return orderRepository.findById(orderId)
            .map(order -> order.getOwnerEmail().equals(email))
            .orElse(false);
    }
}

