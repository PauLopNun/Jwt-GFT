package com.exampleinyection.jwtgft.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    default Optional<User> findByEmail(String email) {
        return findByUsername(email);
    }

    boolean existsByUsername(String username);

    default boolean existsByEmail(String email) {
        return existsByUsername(email);
    }
}

