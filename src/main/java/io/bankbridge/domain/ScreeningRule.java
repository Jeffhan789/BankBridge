package io.bankbridge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "screening_rules")
public class ScreeningRule extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ScreeningField field;
    @Column(nullable = false, length = 128)
    private String matchValue;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ScreeningAction action;
    @Column(nullable = false)
    private boolean active;

    protected ScreeningRule() {}
    public ScreeningRule(String name, ScreeningField field, String matchValue,
                         ScreeningAction action, boolean active) {
        this.name = name;
        this.field = field;
        this.matchValue = matchValue;
        this.action = action;
        this.active = active;
    }
    public String getName() { return name; }
    public ScreeningField getField() { return field; }
    public String getMatchValue() { return matchValue; }
    public ScreeningAction getAction() { return action; }
    public boolean isActive() { return active; }
}
