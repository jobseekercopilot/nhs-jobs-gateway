package com.jobseekercopilot.nhsjobsgateway.model;

import java.util.List;

public record NhsJobsSearchResponse(
        String provider,
        String status,
        int totalResults,
        int totalPages,
        int page,
        int resultsPerPage,
        ProviderAttribution attribution,
        List<CanonicalJob> jobs) {

    public static NhsJobsSearchResponse available(
            int totalResults,
            int totalPages,
            int page,
            int resultsPerPage,
            List<CanonicalJob> jobs) {
        return new NhsJobsSearchResponse(
                "NHS_JOBS",
                "AVAILABLE",
                totalResults,
                totalPages,
                page,
                resultsPerPage,
                ProviderAttribution.nhsJobs(),
                List.copyOf(jobs));
    }

    public static NhsJobsSearchResponse disabled(int page, int resultsPerPage) {
        return new NhsJobsSearchResponse(
                "NHS_JOBS",
                "DISABLED",
                0,
                0,
                page,
                resultsPerPage,
                ProviderAttribution.nhsJobs(),
                List.of());
    }
}
