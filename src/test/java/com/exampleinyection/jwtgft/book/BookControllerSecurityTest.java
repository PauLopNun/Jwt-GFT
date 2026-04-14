package com.exampleinyection.jwtgft.book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getBooksPublicReturns200() throws Exception {
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk());
    }

    @Test
    void createBookWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType("application/json")
                .content("""
                    {
                      "title": "Clean Code",
                      "author": "Robert Martin",
                      "isbn": "978-0132350884",
                      "price": 35.0,
                      "stock": 10
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createBookWithInvalidTokenReturns401ProblemDetail() throws Exception {
        mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer invalid.token.value")
                .contentType("application/json")
                .content("""
                    {
                      "title": "Clean Code",
                      "author": "Robert Martin",
                      "isbn": "978-0132350884",
                      "price": 35.0,
                      "stock": 10
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.title").value("Authentication required"));
    }

    @Test
    @WithMockUser(username = "alice@bookstore.dev", roles = {"USER"})
    void createBookAsUserReturns403() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType("application/json")
                .content("""
                    {
                      "title": "Clean Code",
                      "author": "Robert Martin",
                      "isbn": "978-0132350884",
                      "price": 35.0,
                      "stock": 10
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "bob@bookstore.dev", roles = {"ADMIN"})
    void createBookAsAdminReturns201() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType("application/json")
                .content("""
                    {
                      "title": "Refactoring",
                      "author": "Martin Fowler",
                      "isbn": "978-0201485677",
                      "price": 39.0,
                      "stock": 9
                    }
                    """))
            .andExpect(status().isCreated());
    }
}

