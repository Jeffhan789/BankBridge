package io.bankbridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bankbridge.api.PaymentModels;
import io.bankbridge.messaging.SettlementMessage;
import io.bankbridge.service.PaymentSettlementWorker;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PaymentSettlementWorker worker;

    @MockBean RabbitAdmin rabbitAdmin;
    @MockBean RabbitTemplate rabbitTemplate;

    @Test
    void acceptedPaymentIsAcceptedAndSettlesAsynchronously() throws Exception {
        PaymentModels.CreatePaymentRequest request = validRequest("ACCEPT");
        MvcResult postResult = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.ledgerEntries", hasSize(0)))
                .andExpect(jsonPath("$.statusHistory", hasSize(4)))
                .andReturn();

        JsonNode created = objectMapper.readTree(postResult.getResponse().getContentAsString());
        String paymentId = created.get("id").asText();

        // Simulate async settlement via the worker
        worker.handleSettlement(new SettlementMessage(
                paymentId,
                request.messageId(),
                request.debtorAccount(),
                request.creditorAccount(),
                request.amount(),
                request.currency(),
                Instant.now()
        ));

        mockMvc.perform(get("/api/payments/" + paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SETTLED"))
                .andExpect(jsonPath("$.ledgerEntries", hasSize(2)))
                .andExpect(jsonPath("$.ledgerEntries[0].entryType").value("DEBIT"))
                .andExpect(jsonPath("$.ledgerEntries[1].entryType").value("CREDIT"))
                .andExpect(jsonPath("$.statusHistory", hasSize(6)));
    }

    @Test
    void blockedCreditorIsRejectedWithoutLedgerEntries() throws Exception {
        PaymentModels.CreatePaymentRequest base = validRequest("REJECT");
        PaymentModels.CreatePaymentRequest request = new PaymentModels.CreatePaymentRequest(
                base.messageId(), base.debtorAccount(), "TEST-BLOCKED-001", base.amount(),
                base.currency(), base.destinationCountry(), base.purposeCode(), base.requestedExecutionDate());
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.screening.matchedRuleId").isNotEmpty())
                .andExpect(jsonPath("$.ledgerEntries", hasSize(0)));
    }

    @Test
    void syntheticCountryRuleRoutesPaymentToManualReview() throws Exception {
        PaymentModels.CreatePaymentRequest base = validRequest("REVIEW");
        PaymentModels.CreatePaymentRequest request = new PaymentModels.CreatePaymentRequest(
                base.messageId(), base.debtorAccount(), base.creditorAccount(), base.amount(),
                base.currency(), "ZZ", base.purposeCode(), base.requestedExecutionDate());
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("MANUAL_REVIEW"))
                .andExpect(jsonPath("$.ledgerEntries", hasSize(0)));
    }

    @Test
    void duplicateMessageIdReturnsConflict() throws Exception {
        byte[] body = objectMapper.writeValueAsBytes(validRequest("DUPLICATE"));
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate payment message"));
    }

    @Test
    void invalidAmountReturnsBadRequest() throws Exception {
        PaymentModels.CreatePaymentRequest base = validRequest("INVALID");
        PaymentModels.CreatePaymentRequest request = new PaymentModels.CreatePaymentRequest(
                base.messageId(), base.debtorAccount(), base.creditorAccount(), BigDecimal.ZERO,
                base.currency(), base.destinationCountry(), base.purposeCode(), base.requestedExecutionDate());
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void batchIsolatesInvalidRowsAndCompletesWithErrors() throws Exception {
        String prefix = UUID.randomUUID().toString().substring(0, 8);
        String date = LocalDate.now().plusDays(1).toString();
        String csv = "messageId,debtorAccount,creditorAccount,amount,currency,destinationCountry,purposeCode,requestedExecutionDate\n"
                + prefix + "-OK,TEST-D-1,TEST-C-1,100.00,CNY,SG,GOODS," + date + "\n"
                + prefix + "-BAD,TEST-D-2,TEST-C-2,-1.00,CNY,GB,GOODS," + date + "\n";
        MockMultipartFile file = new MockMultipartFile("file", "payments.csv", "text/csv", csv.getBytes());
        mockMvc.perform(multipart("/api/payment-batches").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED_WITH_ERRORS"))
                .andExpect(jsonPath("$.totalRecords").value(2))
                .andExpect(jsonPath("$.successfulRecords").value(1))
                .andExpect(jsonPath("$.failedRecords").value(1));
    }

    @Test
    void reconciliationRemainsBalancedAfterAsyncSettlement() throws Exception {
        PaymentModels.CreatePaymentRequest request = validRequest("RECON");
        MvcResult postResult = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(postResult.getResponse().getContentAsString());
        String paymentId = created.get("id").asText();

        worker.handleSettlement(new SettlementMessage(
                paymentId,
                request.messageId(),
                request.debtorAccount(),
                request.creditorAccount(),
                request.amount(),
                request.currency(),
                Instant.now()
        ));

        String response = mockMvc.perform(get("/api/reconciliation/daily")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanced").value(true))
                .andReturn().getResponse().getContentAsString();
        JsonNode report = objectMapper.readTree(response);
        org.junit.jupiter.api.Assertions.assertEquals(
                report.get("debitTotal").decimalValue(), report.get("creditTotal").decimalValue());
    }

    private PaymentModels.CreatePaymentRequest validRequest(String scenario) {
        String token = scenario + "-" + UUID.randomUUID().toString().substring(0, 8);
        return new PaymentModels.CreatePaymentRequest("PAY-" + token, "TEST-DEBTOR-" + token,
                "TEST-CREDITOR-" + token, new BigDecimal("12500.00"), "CNY", "SG",
                "GOODS", LocalDate.now().plusDays(1));
    }
}
