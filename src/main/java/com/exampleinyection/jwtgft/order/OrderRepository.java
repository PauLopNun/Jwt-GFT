package com.exampleinyection.jwtgft.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<BookOrder, Long> {

    List<BookOrder> findByOwnerEmail(String ownerEmail);
}

