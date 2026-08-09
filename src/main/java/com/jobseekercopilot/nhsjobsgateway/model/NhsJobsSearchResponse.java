package com.jobseekercopilot.nhsjobsgateway.model;

import java.util.List;

public record NhsJobsSearchResponse(
        String provider,
        int totalAvailable,
        int totalPages,
        int page,
        int resultsPerPage,
        List<NhsJob> jobs) {
    public NhsJobsSearchResponse(int totalAvailable, int totalPages, int page, int resultsPerPage, List<NhsJob> jobs) {
        this("NHS_JOBS", totalAvailable, totalPages, page, resultsPerPage, jobs);
    }
}
