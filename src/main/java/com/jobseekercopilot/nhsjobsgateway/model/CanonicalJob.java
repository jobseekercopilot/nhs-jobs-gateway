package com.jobseekercopilot.nhsjobsgateway.model;

import java.util.List;

public record CanonicalJob(
        String externalJobId,
        String reference,
        String title,
        String employer,
        String description,
        List<String> locations,
        String salaryText,
        String contractType,
        String postedAt,
        String closesAt,
        String source,
        String sourceUrl,
        String applicationUrl) {
}
