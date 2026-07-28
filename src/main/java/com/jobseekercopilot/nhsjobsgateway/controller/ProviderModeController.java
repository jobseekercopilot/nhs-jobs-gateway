package com.jobseekercopilot.nhsjobsgateway.controller;

import com.jobseekercopilot.nhsjobsgateway.config.ExternalProviderMode;
import com.jobseekercopilot.nhsjobsgateway.config.ExternalProviderProperties;
import com.jobseekercopilot.nhsjobsgateway.config.FixtureProperties;
import com.jobseekercopilot.nhsjobsgateway.config.NhsJobsProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class ProviderModeController {
    private final ExternalProviderProperties provider;
    private final FixtureProperties fixture;
    private final NhsJobsProperties nhsJobs;

    public ProviderModeController(
            ExternalProviderProperties provider,
            FixtureProperties fixture,
            NhsJobsProperties nhsJobs) {
        this.provider = provider;
        this.fixture = fixture;
        this.nhsJobs = nhsJobs;
    }

    @GetMapping("/provider-mode")
    public Map<String, Object> providerMode() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gateway", "nhs-jobs-gateway");
        result.put("provider", "NHS_JOBS");
        result.put("mode", provider.getMode().name());
        result.put("datasetId", fixture.getDatasetId());
        result.put("datasetVersion", fixture.getDatasetVersion());
        result.put("scenario", fixture.getScenario());
        result.put(
                "externalCallsEnabled",
                provider.getMode() == ExternalProviderMode.LIVE && nhsJobs.isEnabled());
        result.put("betaReleaseApproved", false);
        return result;
    }
}
