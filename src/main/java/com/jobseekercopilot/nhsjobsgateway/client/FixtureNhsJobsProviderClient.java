package com.jobseekercopilot.nhsjobsgateway.client;

import com.jobseekercopilot.nhsjobsgateway.model.NhsJob;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchRequest;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchResponse;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "external-provider", name = "mode", havingValue = "FIXTURE", matchIfMissing = true)
public class FixtureNhsJobsProviderClient implements NhsJobsProviderClient {
    @Override public NhsJobsSearchResponse search(NhsJobsSearchRequest request) {
        int page = request.page() == null ? 1 : request.page();
        int size = request.resultsPerPage() == null ? 10 : request.resultsPerPage();
        NhsJob job = new NhsJob("nhs-fixture-1", "NHS-FIXTURE-1", "Community Staff Nurse",
                "Example NHS Foundation Trust", "Provide safe, compassionate community nursing care.",
                List.of("Leeds, LS1 1AA"), "£30,000 to £36,000 a year", new BigDecimal("30000"),
                new BigDecimal("36000"), "GBP", "YEAR", "Permanent", "2026-08-01T09:00:00Z",
                "2026-08-31", "https://www.jobs.nhs.uk/candidate/jobadvert/NHS-FIXTURE-1");
        return new NhsJobsSearchResponse(1, 1, page, size, List.of(job));
    }
}
