package io.bankbridge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_status_events")
public class PaymentStatusEvent extends BaseEntity {
    @Column(nullable = false, length = 36)
    private String paymentId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PaymentStatus status;
    @Column(nullable = false, length = 255)
    private String reason;

    protected PaymentStatusEvent() {}
    public PaymentStatusEvent(String paymentId, PaymentStatus status, String reason) {
        this.paymentId = paymentId;
        this.status = status;
        this.reason = reason;
    }
    public String getPaymentId() { return paymentId; }
    public PaymentStatus getStatus() { return status; }
    public String getReason() { return reason; }
}
