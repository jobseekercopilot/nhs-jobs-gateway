package com.jobseekercopilot.nhsjobsgateway.controller;

import com.jobseekercopilot.nhsjobsgateway.client.NhsJobsProviderException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProviderExceptionHandler {
    @ExceptionHandler(NhsJobsProviderException.class)
    ResponseEntity<Map<String, String>> providerFailure(NhsJobsProviderException exception) {
        HttpStatus status = exception.getStatus();
        String code = status == HttpStatus.TOO_MANY_REQUESTS
                ? "RATE_LIMITED"
                : "PROVIDER_UNAVAILABLE";
        String message = status == HttpStatus.TOO_MANY_REQUESTS
                ? "NHS Jobs rate limit reached"
                : "NHS Jobs is temporarily unavailable";
        return ResponseEntity.status(status).body(Map.of(
                "provider", "NHS_JOBS",
                "code", code,
                "message", message));
    }
}
