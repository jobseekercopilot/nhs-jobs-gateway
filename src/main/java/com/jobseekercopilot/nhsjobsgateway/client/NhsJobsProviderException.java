package com.jobseekercopilot.nhsjobsgateway.client;

import org.springframework.http.HttpStatus;

public class NhsJobsProviderException extends RuntimeException {
    private final HttpStatus status;
    public NhsJobsProviderException(String message, Throwable cause) { this(message, cause, HttpStatus.SERVICE_UNAVAILABLE); }
    public NhsJobsProviderException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, false, false);
        this.status = status;
    }
    public HttpStatus getStatus() { return status; }
}
