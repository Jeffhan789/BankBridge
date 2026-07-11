package io.bankbridge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reconciliation_records")
public class ReconciliationRecord extends BaseEntity {
    @Column(nullable = false)
    private LocalDate businessDate;
    @Column(nullable = false)
    private long settledPayments;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal debitTotal;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal creditTotal;
    @Column(nullable = false)
    private boolean balanced;

    protected ReconciliationRecord() {}
    public ReconciliationRecord(LocalDate businessDate, long settledPayments,
                                BigDecimal debitTotal, BigDecimal creditTotal) {
        this.businessDate = businessDate;
        this.settledPayments = settledPayments;
        this.debitTotal = debitTotal;
        this.creditTotal = creditTotal;
        this.balanced = debitTotal.compareTo(creditTotal) == 0;
    }
    public LocalDate getBusinessDate() { return businessDate; }
    public long getSettledPayments() { return settledPayments; }
    public BigDecimal getDebitTotal() { return debitTotal; }
    public BigDecimal getCreditTotal() { return creditTotal; }
    public boolean isBalanced() { return balanced; }
}
