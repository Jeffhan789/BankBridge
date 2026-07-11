package io.bankbridge.api;

import io.bankbridge.service.ReportingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class ReportingController {
    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/reconciliation/daily")
    public ReportModels.ReconciliationResponse reconcile(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportingService.reconcile(date == null ? LocalDate.now() : date);
    }

    @GetMapping("/compliance-reports/daily")
    public ReportModels.ComplianceReport compliance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportingService.compliance(date == null ? LocalDate.now() : date);
    }
}
