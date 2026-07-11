package io.bankbridge.api;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
