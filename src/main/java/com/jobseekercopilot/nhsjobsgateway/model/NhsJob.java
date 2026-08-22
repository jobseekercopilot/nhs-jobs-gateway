package com.jobseekercopilot.nhsjobsgateway.model;

import java.math.BigDecimal;
import java.util.List;

public record NhsJob(
        String externalJobId,
        String reference,
        String title,
        String employer,
        String description,
        List<String> locations,
        String salaryText,
        BigDecimal salaryMinimum,
        BigDecimal salaryMaximum,
        String salaryCurrency,
        String salaryPeriod,
        String contractType,
        String postedAt,
        String closingDate,
        String sourceUrl) {}
