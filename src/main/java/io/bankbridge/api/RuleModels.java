package io.bankbridge.api;

import io.bankbridge.domain.ScreeningAction;
import io.bankbridge.domain.ScreeningField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class RuleModels {
    private RuleModels() {}
    public record CreateRuleRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull ScreeningField field,
            @NotBlank @Size(max = 128) String matchValue,
            @NotNull ScreeningAction action,
            boolean active
    ) {}
    public record RuleResponse(String id, String name, ScreeningField field, String matchValue,
                               ScreeningAction action, boolean active, Instant createdAt) {}
}
