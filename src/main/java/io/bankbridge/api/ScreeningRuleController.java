package io.bankbridge.api;

import io.bankbridge.service.ScreeningRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/screening-rules")
public class ScreeningRuleController {
    private final ScreeningRuleService service;

    public ScreeningRuleController(ScreeningRuleService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RuleModels.RuleResponse create(@Valid @RequestBody RuleModels.CreateRuleRequest request) {
        return service.create(request);
    }

    @PreAuthorize("hasAnyRole('OPERATOR', 'COMPLIANCE_ANALYST', 'AUDITOR', 'ADMIN')")
    @GetMapping
    public List<RuleModels.RuleResponse> list() {
        return service.list();
    }
}
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/screening-rules")
public class ScreeningRuleController {
    private final ScreeningRuleService service;

    public ScreeningRuleController(ScreeningRuleService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RuleModels.RuleResponse create(@Valid @RequestBody RuleModels.CreateRuleRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<RuleModels.RuleResponse> list() {
        return service.list();
    }
}
