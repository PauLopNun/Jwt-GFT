package com.exampleinyection.jwtgft.order;

import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PreAuthorize("isAuthenticated()")
    public List<BookOrder> getMyOrders(String currentUserEmail) {
        return orderRepository.findByOwnerEmail(currentUserEmail);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public BookOrder placeOrder(String currentUserEmail, OrderRequest request) {
        BookOrder order = new BookOrder();
        order.setOwnerEmail(currentUserEmail);
        order.setBookId(request.bookId());
        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isOwner(#id, authentication.name)")
    public Optional<BookOrder> getOrder(Long id) {
        return orderRepository.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isOwner(#id, authentication.name)")
    public Optional<BookOrder> cancelOrder(Long id) {
        return orderRepository.findById(id).map(order -> {
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new IllegalStateException("Order cannot be cancelled unless it is PENDING");
            }
            order.setStatus(OrderStatus.CANCELLED);
            return orderRepository.save(order);
        });
    }
}

