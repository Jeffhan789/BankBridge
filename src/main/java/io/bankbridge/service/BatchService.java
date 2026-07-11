package io.bankbridge.service;

import io.bankbridge.api.BadRequestException;
import io.bankbridge.api.BatchModels;
import io.bankbridge.api.PaymentModels;
import io.bankbridge.api.ResourceNotFoundException;
import io.bankbridge.domain.BatchJob;
import io.bankbridge.repository.BatchJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BatchService {
    private static final String EXPECTED_HEADER =
            "messageId,debtorAccount,creditorAccount,amount,currency,destinationCountry,purposeCode,requestedExecutionDate";

    private final BatchJobRepository batchRepository;
    private final PaymentService paymentService;

    public BatchService(BatchJobRepository batchRepository, PaymentService paymentService) {
        this.batchRepository = batchRepository;
        this.paymentService = paymentService;
    }

    public BatchModels.BatchResponse process(MultipartFile file) {
        if (file.isEmpty()) throw new BadRequestException("CSV file must not be empty");
        String fileName = file.getOriginalFilename() == null ? "payments.csv" : file.getOriginalFilename();
        BatchJob job = batchRepository.save(new BatchJob(fileName));
        job.markProcessing();
        batchRepository.save(job);

        int total = 0;
        int successful = 0;
        List<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || !header.trim().equals(EXPECTED_HEADER)) {
                throw new BadRequestException("CSV header must be: " + EXPECTED_HEADER);
            }
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                total++;
                try {
                    String[] value = line.split(",", -1);
                    if (value.length != 8) throw new IllegalArgumentException("Expected 8 columns");
                    paymentService.create(new PaymentModels.CreatePaymentRequest(
                            value[0].trim(), value[1].trim(), value[2].trim(),
                            new BigDecimal(value[3].trim()), value[4].trim(), value[5].trim(),
                            value[6].trim(), LocalDate.parse(value[7].trim())));
                    successful++;
                } catch (RuntimeException exception) {
                    errors.add("line " + lineNumber + ": " + exception.getMessage());
                }
            }
            job.complete(total, successful, total - successful, String.join(" | ", errors));
        } catch (BadRequestException exception) {
            job.fail(exception.getMessage());
            batchRepository.save(job);
            throw exception;
        } catch (Exception exception) {
            job.fail("Unable to process CSV: " + exception.getMessage());
            batchRepository.save(job);
            throw new BadRequestException(job.getErrorSummary());
        }
        return response(batchRepository.save(job));
    }

    public BatchModels.BatchResponse get(String id) {
        return response(batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch job not found: " + id)));
    }

    private BatchModels.BatchResponse response(BatchJob job) {
        return new BatchModels.BatchResponse(job.getId(), job.getFileName(), job.getStatus(),
                job.getTotalRecords(), job.getSuccessfulRecords(), job.getFailedRecords(),
                job.getErrorSummary(), job.getCreatedAt(), job.getUpdatedAt());
    }
}
