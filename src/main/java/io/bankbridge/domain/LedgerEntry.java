package io.bankbridge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry extends BaseEntity {
    @Column(nullable = false, length = 36)
    private String paymentId;
    @Column(nullable = false, length = 64)
    private String ledgerAccount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private EntryType entryType;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;

    protected LedgerEntry() {}
    public LedgerEntry(String paymentId, String ledgerAccount, EntryType entryType,
                       BigDecimal amount, String currency) {
        this.paymentId = paymentId;
        this.ledgerAccount = ledgerAccount;
        this.entryType = entryType;
        this.amount = amount;
        this.currency = currency;
    }
    public String getPaymentId() { return paymentId; }
    public String getLedgerAccount() { return ledgerAccount; }
    public EntryType getEntryType() { return entryType; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
