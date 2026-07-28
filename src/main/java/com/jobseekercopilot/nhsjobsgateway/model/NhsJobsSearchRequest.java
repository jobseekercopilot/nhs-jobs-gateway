package com.jobseekercopilot.nhsjobsgateway.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

public record NhsJobsSearchRequest(
        @Size(max = 200) String keyword,
        @Size(max = 200) String location,
        @Min(0) @Max(100) Integer distanceMiles,
        @Size(max = 3) String countryCode,
        @Size(max = 20) List<@Size(max = 80) String> contractTypes,
        @Size(max = 20) List<@Size(max = 80) String> staffGroups,
        @Size(max = 20) List<@Size(max = 80) String> workingPatterns,
        @Size(max = 20) List<@Size(max = 80) String> payBands,
        @Min(0) Integer salaryFrom,
        @Min(0) Integer salaryTo,
        @Size(max = 40) String publishedFrom,
        @Size(max = 40) String sort,
        @Min(1) Integer page,
        @Min(1) @Max(100) Integer resultsPerPage) {

    public int resolvedPage() {
        return page == null ? 1 : page;
    }

    public int resolvedResultsPerPage(int defaultValue) {
        return resultsPerPage == null ? defaultValue : resultsPerPage;
    }
}
