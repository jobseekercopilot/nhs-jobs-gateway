package com.jobseekercopilot.nhsjobsgateway.controller;

import com.jobseekercopilot.nhsjobsgateway.client.NhsJobsProviderClient;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchRequest;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nhs/jobs")
@Tag(name = "NHS Jobs")
public class NhsJobsSearchController {
    private final NhsJobsProviderClient providerClient;

    public NhsJobsSearchController(NhsJobsProviderClient providerClient) {
        this.providerClient = providerClient;
    }

    @PostMapping("/search")
    @Operation(summary = "Search public NHS Jobs vacancies")
    public ResponseEntity<NhsJobsSearchResponse> search(
            @Valid @RequestBody NhsJobsSearchRequest request) {
        return ResponseEntity.ok(providerClient.search(request));
    }
}
