package com.jobseekercopilot.nhsjobsgateway.config;

import java.net.URI;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProviderModeSafety implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ProviderModeSafety.class);
    private final ExternalProviderProperties provider;
    private final NhsJobsProperties nhsJobs;
    private final FixtureProperties fixture;
    private final Environment environment;

    public ProviderModeSafety(
            ExternalProviderProperties provider,
            NhsJobsProperties nhsJobs,
            FixtureProperties fixture,
            Environment environment) {
        this.provider = provider;
        this.nhsJobs = nhsJobs;
        this.fixture = fixture;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean production = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod")
                        || profile.equalsIgnoreCase("production"));
        if (production && provider.getMode() == ExternalProviderMode.FIXTURE) {
            throw new IllegalStateException(
                    "nhs-jobs-gateway cannot start in FIXTURE mode with a production profile.");
        }
        if (provider.getMode() == ExternalProviderMode.LIVE && nhsJobs.isEnabled()) {
            requireOfficialOrigin(nhsJobs.getBaseUrl());
            if (!"/api/v1/search_xml".equals(nhsJobs.getSearchPath())) {
                throw new IllegalStateException(
                        "NHS Jobs LIVE mode permits only the documented /api/v1/search_xml path.");
            }
        }
        log.info(
                "provider mode active gateway=nhs-jobs-gateway mode={} datasetId={} datasetVersion={} scenario={} externalCallsEnabled={}",
                provider.getMode(),
                fixture.getDatasetId(),
                fixture.getDatasetVersion(),
                fixture.getScenario(),
                provider.getMode() == ExternalProviderMode.LIVE && nhsJobs.isEnabled());
    }

    private void requireOfficialOrigin(String baseUrl) {
        URI uri;
        try {
            uri = URI.create(baseUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("NHS Jobs LIVE base URL is invalid.", exception);
        }
        boolean official = "https".equalsIgnoreCase(uri.getScheme())
                && "www.jobs.nhs.uk".equalsIgnoreCase(uri.getHost())
                && uri.getUserInfo() == null
                && (uri.getPort() == -1 || uri.getPort() == 443)
                && (uri.getPath() == null || uri.getPath().isBlank() || "/".equals(uri.getPath()));
        if (!official) {
            throw new IllegalStateException(
                    "NHS Jobs LIVE mode permits only the official https://www.jobs.nhs.uk origin.");
        }
    }
}
