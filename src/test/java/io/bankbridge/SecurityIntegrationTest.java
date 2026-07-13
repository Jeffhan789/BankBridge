package io.bankbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operator_canAccessPaymentEndpoints() throws Exception {
        SecurityTestBase security = new SecurityTestBase(mockMvc, objectMapper);
        String token = security.loginAs("operator001", "operator001");

        mockMvc.perform(get("/api/payments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void operator_cannotAccessAdminEndpoints_returns403() throws Exception {
        SecurityTestBase security = new SecurityTestBase(mockMvc, objectMapper);
        String token = security.loginAs("operator001", "operator001");

        mockMvc.perform(post("/api/screening-rules")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"name\":\"TEST\",\"field\":\"DEBTOR_ACCOUNT\",\"matchValue\":\"X\",\"action\":\"REJECT\",\"active\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_canAccessAllEndpoints() throws Exception {
        SecurityTestBase security = new SecurityTestBase(mockMvc, objectMapper);
        String token = security.loginAs("admin001", "admin001");

        mockMvc.perform(get("/api/payments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/audit-events")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void auditor_canAccessAuditEvents() throws Exception {
        SecurityTestBase security = new SecurityTestBase(mockMvc, objectMapper);
        String token = security.loginAs("auditor001", "auditor001");

        mockMvc.perform(get("/api/audit-events")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void complianceAnalyst_canAccessAuditEvents() throws Exception {
        SecurityTestBase security = new SecurityTestBase(mockMvc, objectMapper);
        String token = security.loginAs("compliance001", "compliance001");

        mockMvc.perform(get("/api/audit-events")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
