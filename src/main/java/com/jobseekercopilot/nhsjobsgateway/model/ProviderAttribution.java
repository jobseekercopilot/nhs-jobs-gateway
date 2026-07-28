package com.jobseekercopilot.nhsjobsgateway.model;

public record ProviderAttribution(
        String label,
        String sourceUrl,
        String licenceUrl,
        String disclaimer) {

    public static ProviderAttribution nhsJobs() {
        return new ProviderAttribution(
                "Vacancy source: NHS Jobs",
                "https://www.jobs.nhs.uk/",
                "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/",
                "NHS Jobs does not endorse Job Seeker Copilot.");
    }
}
