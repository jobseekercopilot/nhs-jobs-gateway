package com.jobseekercopilot.nhsjobsgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "nhs-jobs")
public class NhsJobsProperties {
    private String baseUrl = "https://www.jobs.nhs.uk/api/v1/search_xml";
    private boolean enabled = true;
    private int resultsPerPage = 50;
    private String defaultCountryCode = "GB-ENG";
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getResultsPerPage() { return Math.max(1, Math.min(100, resultsPerPage)); }
    public void setResultsPerPage(int resultsPerPage) { this.resultsPerPage = resultsPerPage; }
    public String getDefaultCountryCode() { return defaultCountryCode; }
    public void setDefaultCountryCode(String defaultCountryCode) { this.defaultCountryCode = defaultCountryCode; }
}
