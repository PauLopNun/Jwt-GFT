package com.exampleinyection.jwtgft.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerUserDetailsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails("alice@bookstore.dev")
    void getMyOrdersWithRealUserDetailsReturns200() throws Exception {
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk());
    }
}

