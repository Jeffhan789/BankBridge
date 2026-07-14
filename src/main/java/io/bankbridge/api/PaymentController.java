package io.bankbridge.api;

import io.bankbridge.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentModels.PaymentResponse create(@Valid @RequestBody PaymentModels.CreatePaymentRequest request) {
        return paymentService.create(request);
    }

    @PreAuthorize("hasAnyRole('OPERATOR', 'COMPLIANCE_ANALYST', 'AUDITOR', 'ADMIN')")
    @GetMapping
    public List<PaymentModels.PaymentResponse> list() {
        return paymentService.list();
    }

    @PreAuthorize("hasAnyRole('OPERATOR', 'COMPLIANCE_ANALYST', 'AUDITOR', 'ADMIN')")
    @GetMapping("/{id}")
    public PaymentModels.PaymentResponse get(@PathVariable String id) {
        return paymentService.get(id);
    }
}
