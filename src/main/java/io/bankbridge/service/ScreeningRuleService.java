package io.bankbridge.service;

import io.bankbridge.api.RuleModels;
import io.bankbridge.domain.ScreeningRule;
import io.bankbridge.repository.AuditEventRepository;
import io.bankbridge.repository.ScreeningRuleRepository;
import io.bankbridge.domain.AuditEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScreeningRuleService {
    private final ScreeningRuleRepository repository;
    private final AuditEventRepository auditRepository;

    public ScreeningRuleService(ScreeningRuleRepository repository, AuditEventRepository auditRepository) {
        this.repository = repository;
        this.auditRepository = auditRepository;
    }

    @Transactional
    public RuleModels.RuleResponse create(RuleModels.CreateRuleRequest request) {
        ScreeningRule rule = repository.save(new ScreeningRule(request.name().trim(), request.field(),
                request.matchValue().trim(), request.action(), request.active()));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String actorUsername = auth != null ? auth.getName() : null;
        String actorRole = auth != null && !auth.getAuthorities().isEmpty()
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : null;
        auditRepository.save(new AuditEvent("SCREENING_RULE", rule.getId(), "RULE_CREATED",
                "Synthetic screening rule created: " + rule.getName(), actorUsername, actorRole));
        return response(rule);
    }

    @Transactional(readOnly = true)
    public List<RuleModels.RuleResponse> list() {
        return repository.findAll().stream().map(this::response).toList();
    }

    private RuleModels.RuleResponse response(ScreeningRule rule) {
        return new RuleModels.RuleResponse(rule.getId(), rule.getName(), rule.getField(),
                rule.getMatchValue(), rule.getAction(), rule.isActive(), rule.getCreatedAt());
    }
}
