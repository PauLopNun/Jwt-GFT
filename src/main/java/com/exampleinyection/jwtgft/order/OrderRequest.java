package com.exampleinyection.jwtgft.order;

import jakarta.validation.constraints.NotNull;

public record OrderRequest(
    @NotNull(message = "bookId is required")
    Long bookId
) {
}

