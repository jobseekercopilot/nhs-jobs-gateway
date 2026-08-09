package com.jobseekercopilot.nhsjobsgateway.model;

import java.util.List;

public record NhsJobsSearchRequest(
        String targetRole,
        String location,
        Integer distanceMiles,
        String countryCode,
        List<String> contractTypes,
        List<String> workingPatterns,
        Integer salaryMinimum,
        Integer salaryMaximum,
        Integer postedWithinDays,
        Integer page,
        Integer resultsPerPage) {}
