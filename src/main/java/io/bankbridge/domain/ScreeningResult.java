package io.bankbridge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "screening_results")
public class ScreeningResult extends BaseEntity {
    @Column(nullable = false, length = 36)
    private String paymentId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PaymentStatus decision;
    @Column(length = 36)
    private String matchedRuleId;
    @Column(nullable = false, length = 255)
    private String reason;

    protected ScreeningResult() {}
    public ScreeningResult(String paymentId, PaymentStatus decision, String matchedRuleId, String reason) {
        this.paymentId = paymentId;
        this.decision = decision;
        this.matchedRuleId = matchedRuleId;
        this.reason = reason;
    }
    public String getPaymentId() { return paymentId; }
    public PaymentStatus getDecision() { return decision; }
    public String getMatchedRuleId() { return matchedRuleId; }
    public String getReason() { return reason; }
}
