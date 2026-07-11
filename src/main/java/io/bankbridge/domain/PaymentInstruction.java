package io.bankbridge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_instructions")
public class PaymentInstruction extends BaseEntity {
    @Column(nullable = false, unique = true, length = 64)
    private String messageId;
    @Column(nullable = false, length = 64)
    private String debtorAccount;
    @Column(nullable = false, length = 64)
    private String creditorAccount;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(nullable = false, length = 2)
    private String destinationCountry;
    @Column(nullable = false, length = 32)
    private String purposeCode;
    @Column(nullable = false)
    private LocalDate requestedExecutionDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PaymentStatus status;
    @Column(length = 255)
    private String statusReason;

    protected PaymentInstruction() {}

    public PaymentInstruction(String messageId, String debtorAccount, String creditorAccount,
                              BigDecimal amount, String currency, String destinationCountry,
                              String purposeCode, LocalDate requestedExecutionDate) {
        this.messageId = messageId;
        this.debtorAccount = debtorAccount;
        this.creditorAccount = creditorAccount;
        this.amount = amount;
        this.currency = currency;
        this.destinationCountry = destinationCountry;
        this.purposeCode = purposeCode;
        this.requestedExecutionDate = requestedExecutionDate;
        this.status = PaymentStatus.RECEIVED;
    }

    public String getMessageId() { return messageId; }
    public String getDebtorAccount() { return debtorAccount; }
    public String getCreditorAccount() { return creditorAccount; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDestinationCountry() { return destinationCountry; }
    public String getPurposeCode() { return purposeCode; }
    public LocalDate getRequestedExecutionDate() { return requestedExecutionDate; }
    public PaymentStatus getStatus() { return status; }
    public String getStatusReason() { return statusReason; }
    public void setStatus(PaymentStatus status, String reason) {
        this.status = status;
        this.statusReason = reason;
    }
}
