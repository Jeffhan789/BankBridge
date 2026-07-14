package io.bankbridge.api;

import io.bankbridge.service.BatchService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/payment-batches")
public class BatchController {
    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public BatchModels.BatchResponse upload(@RequestParam("file") MultipartFile file) {
        return batchService.process(file);
    }

    @PreAuthorize("hasAnyRole('OPERATOR', 'COMPLIANCE_ANALYST', 'AUDITOR', 'ADMIN')")
    @GetMapping("/{id}")
    public BatchModels.BatchResponse get(@PathVariable String id) {
        return batchService.get(id);
    }
}
