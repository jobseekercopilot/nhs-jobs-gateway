package com.jobseekercopilot.nhsjobsgateway.controller;

import com.jobseekercopilot.nhsjobsgateway.client.NhsJobsProviderClient;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchRequest;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nhs-jobs/jobs")
public class NhsJobsSearchController {
    private final NhsJobsProviderClient provider;
    public NhsJobsSearchController(NhsJobsProviderClient provider) { this.provider = provider; }
    @PostMapping("/search") public NhsJobsSearchResponse search(@RequestBody NhsJobsSearchRequest request) { return provider.search(request); }
}
