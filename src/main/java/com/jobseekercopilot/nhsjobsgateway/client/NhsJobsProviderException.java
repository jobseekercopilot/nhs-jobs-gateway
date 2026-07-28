package com.jobseekercopilot.nhsjobsgateway.client;

import org.springframework.http.HttpStatus;

public class NhsJobsProviderException extends RuntimeException {
    private final HttpStatus status;

    public NhsJobsProviderException(String message) {
        this(message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public NhsJobsProviderException(String message, HttpStatus status) {
        super(message, null, false, false);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
