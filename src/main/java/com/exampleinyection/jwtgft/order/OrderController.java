package com.exampleinyection.jwtgft.order;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<BookOrder>> getMyOrders(@AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(orderService.getMyOrders(currentUser.getUsername()));
    }

    @PostMapping
    public ResponseEntity<BookOrder> placeOrder(
        @Valid @RequestBody OrderRequest request,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        BookOrder created = orderService.placeOrder(currentUser.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookOrder> getOrder(@PathVariable Long id) {
        return orderService.getOrder(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/cancel")
    public ResponseEntity<BookOrder> cancelOrderGet(@PathVariable Long id) {
        return orderService.cancelOrder(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookOrder> cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}

