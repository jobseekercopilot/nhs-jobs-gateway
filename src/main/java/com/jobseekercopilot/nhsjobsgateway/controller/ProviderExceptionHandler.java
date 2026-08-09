package com.jobseekercopilot.nhsjobsgateway.controller;

import com.jobseekercopilot.nhsjobsgateway.client.NhsJobsProviderException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProviderExceptionHandler {
    @ExceptionHandler(NhsJobsProviderException.class)
    ResponseEntity<Map<String, String>> providerFailure(NhsJobsProviderException exception) {
        String code = exception.getStatus().value() == 429 ? "RATE_LIMITED" :
                exception.getStatus().value() == 401 || exception.getStatus().value() == 403 ? "CONFIGURATION_ERROR" : "PROVIDER_UNAVAILABLE";
        return ResponseEntity.status(exception.getStatus()).body(Map.of("code", code, "message", "NHS Jobs is temporarily unavailable"));
    }
}
