package io.bankbridge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "batch_jobs")
public class BatchJob extends BaseEntity {
    @Column(nullable = false, length = 255)
    private String fileName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BatchStatus status;
    @Column(nullable = false)
    private int totalRecords;
    @Column(nullable = false)
    private int successfulRecords;
    @Column(nullable = false)
    private int failedRecords;
    @Column(length = 2000)
    private String errorSummary;

    protected BatchJob() {}
    public BatchJob(String fileName) {
        this.fileName = fileName;
        this.status = BatchStatus.RECEIVED;
    }
    public String getFileName() { return fileName; }
    public BatchStatus getStatus() { return status; }
    public int getTotalRecords() { return totalRecords; }
    public int getSuccessfulRecords() { return successfulRecords; }
    public int getFailedRecords() { return failedRecords; }
    public String getErrorSummary() { return errorSummary; }
    public void markProcessing() { this.status = BatchStatus.PROCESSING; }
    public void complete(int total, int successful, int failed, String errors) {
        this.totalRecords = total;
        this.successfulRecords = successful;
        this.failedRecords = failed;
        this.errorSummary = errors;
        this.status = failed == 0 ? BatchStatus.COMPLETED : BatchStatus.COMPLETED_WITH_ERRORS;
    }
    public void fail(String error) {
        this.status = BatchStatus.FAILED;
        this.errorSummary = error;
    }
}
