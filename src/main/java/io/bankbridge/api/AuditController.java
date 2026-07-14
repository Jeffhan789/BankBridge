package io.bankbridge.api;

import io.bankbridge.domain.AuditEvent;
import io.bankbridge.repository.AuditEventRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-events")
public class AuditController {
    private final AuditEventRepository repository;

    public AuditController(AuditEventRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasAnyRole('COMPLIANCE_ANALYST', 'AUDITOR', 'ADMIN')")
    @GetMapping
    public List<AuditEvent> list(@RequestParam(required = false) String aggregateId) {
        return aggregateId == null || aggregateId.isBlank()
                ? repository.findTop100ByOrderByCreatedAtDesc()
                : repository.findByAggregateIdOrderByCreatedAtAsc(aggregateId);
    }
}
