package com.jobseekercopilot.nhsjobsgateway.client;

import com.jobseekercopilot.nhsjobsgateway.model.CanonicalJob;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchRequest;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchResponse;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "external-provider",
        name = "mode",
        havingValue = "FIXTURE",
        matchIfMissing = true)
public class FixtureNhsJobsProviderClient implements NhsJobsProviderClient {
    private static final Logger log = LoggerFactory.getLogger(FixtureNhsJobsProviderClient.class);
    private static final List<CanonicalJob> DATASET = List.of(
            job(
                    "C9855-FIXTURE-001",
                    "9855-FIXTURE-001",
                    "Community Staff Nurse",
                    "Northshire Community NHS Foundation Trust",
                    "Support a synthetic community nursing team across Northshire.",
                    List.of("Northshire, NS1 2AB"),
                    "£31,049 to £37,796 a year",
                    "Permanent",
                    "2026-07-20",
                    "2026-08-20"),
            job(
                    "C9274-FIXTURE-002",
                    "9274-FIXTURE-002",
                    "Data Analyst",
                    "Westborough Teaching Hospitals NHS Trust",
                    "Improve synthetic operational reporting for hospital services.",
                    List.of("Westborough, WB3 4CD", "Home or remote"),
                    "£37,338 to £44,962 a year",
                    "Fixed term",
                    "2026-07-21",
                    "2026-08-18"),
            job(
                    "C9123-FIXTURE-003",
                    "9123-FIXTURE-003",
                    "Estates Maintenance Apprentice",
                    "Eastvale NHS Trust",
                    "Learn practical maintenance skills in a synthetic NHS estate.",
                    List.of("Eastvale, EV5 6EF"),
                    "£24,071 a year",
                    "Apprenticeship",
                    "2026-07-22",
                    "2026-08-22"));

    @Override
    public NhsJobsSearchResponse search(NhsJobsSearchRequest request) {
        int page = request.resolvedPage();
        int pageSize = request.resolvedResultsPerPage(50);
        List<CanonicalJob> matches = DATASET.stream()
                .filter(job -> contains(job.title() + " " + job.description(), request.keyword()))
                .filter(job -> contains(String.join(" ", job.locations()), request.location()))
                .toList();
        int from = Math.min(matches.size(), (page - 1) * pageSize);
        int to = Math.min(matches.size(), from + pageSize);
        int totalPages = matches.isEmpty() ? 0 : (matches.size() + pageSize - 1) / pageSize;
        log.info(
                "NHS Jobs fixture returned resultCount={} totalResults={} page={}",
                to - from,
                matches.size(),
                page);
        return NhsJobsSearchResponse.available(
                matches.size(),
                totalPages,
                page,
                pageSize,
                matches.subList(from, to));
    }

    private boolean contains(String searchable, String requested) {
        return requested == null
                || requested.isBlank()
                || searchable.toLowerCase(Locale.ROOT)
                        .contains(requested.toLowerCase(Locale.ROOT));
    }

    private static CanonicalJob job(
            String id,
            String reference,
            String title,
            String employer,
            String description,
            List<String> locations,
            String salary,
            String contract,
            String posted,
            String closes) {
        String link = SafeNhsJobsLink.fixture(id);
        return new CanonicalJob(
                id,
                reference,
                title,
                employer,
                description,
                locations,
                salary,
                contract,
                posted,
                closes,
                "NHS Jobs (synthetic fixture)",
                link,
                link);
    }
}
