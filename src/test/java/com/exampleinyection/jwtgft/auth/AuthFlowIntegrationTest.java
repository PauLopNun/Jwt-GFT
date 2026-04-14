package com.exampleinyection.jwtgft.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAndLoginReturnJwtContract() throws Exception {
        register("carol@bookstore.dev", "test1234", null);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "carol@bookstore.dev",
                      "password": "test1234"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void refreshAndLogoutFlowWorks() throws Exception {
        String email = "refresh_test@bookstore.dev";
        String password = "password123";
        register(email, password, "ROLE_USER");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(email, password)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String accessToken = loginJson.get("accessToken").asText();
        String refreshToken = loginJson.get("refreshToken").asText();

        String refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "refreshToken": "%s"
                    }
                    """.formatted(refreshToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String rotatedRefresh = objectMapper.readTree(refreshResponse).get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "refreshToken": "%s"
                    }
                    """.formatted(rotatedRefresh)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanCreateBookButAnonymousCannot() throws Exception {
        String adminToken = register("admin_test@bookstore.dev", "admin456", "ROLE_ADMIN");

        String bookPayload = """
            {
              "title": "Domain-Driven Design",
              "author": "Eric Evans",
              "isbn": "978-0321125217",
              "price": 42.50,
              "stock": 4
            }
            """;

        mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookPayload))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Refactoring",
                      "author": "Martin Fowler",
                      "isbn": "978-0201485677",
                      "price": 39.90,
                      "stock": 2
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogAndProfileRulesWorkForUserAndAdmin() throws Exception {
        String aliceToken = register("alice_test@bookstore.dev", "password123", "ROLE_USER");
        String bobToken = register("bob_test@bookstore.dev", "admin456", "ROLE_ADMIN");

        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + bobToken))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/alice_test@bookstore.dev")
                .header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/alice_test@bookstore.dev")
                .header("Authorization", "Bearer " + bobToken))
            .andExpect(status().isOk());
    }

    @Test
    void orderOwnershipAndCancelRuleAreEnforced() throws Exception {
        String ownerToken = register("owner_test@bookstore.dev", "password123", "ROLE_USER");
        String otherToken = register("other_test@bookstore.dev", "password123", "ROLE_USER");

        String response = mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "bookId": 1
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/orders/" + orderId)
                .header("Authorization", "Bearer " + ownerToken))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/" + orderId)
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel")
                .header("Authorization", "Bearer " + ownerToken))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel")
                .header("Authorization", "Bearer " + ownerToken))
            .andExpect(status().isBadRequest());
    }

    private String register(String email, String password, String role) throws Exception {
        StringBuilder payload = new StringBuilder();
        payload.append("{\n")
            .append("  \"email\": \"").append(email).append("\",\n")
            .append("  \"password\": \"").append(password).append("\"");
        if (role != null) {
            payload.append(",\n  \"role\": \"").append(role).append("\"");
        }
        payload.append("\n}");

        String response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload.toString()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(900))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("accessToken").asText();
    }
}

