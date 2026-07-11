package io.bankbridge.api;

public class DuplicateMessageException extends RuntimeException {
    public DuplicateMessageException(String message) { super(message); }
}
