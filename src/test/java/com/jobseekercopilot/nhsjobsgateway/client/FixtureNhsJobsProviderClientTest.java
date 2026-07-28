package com.jobseekercopilot.nhsjobsgateway.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchRequest;
import org.junit.jupiter.api.Test;

class FixtureNhsJobsProviderClientTest {
    private final FixtureNhsJobsProviderClient client = new FixtureNhsJobsProviderClient();

    @Test
    void fixtureIsDeterministicClearlySyntheticAndExternallyInert() {
        var first = client.search(request("nurse", null));
        var second = client.search(request("nurse", null));

        assertThat(first).isEqualTo(second);
        assertThat(first.jobs()).singleElement().satisfies(job -> {
            assertThat(job.externalJobId()).contains("FIXTURE");
            assertThat(job.employer()).contains("NHS");
            assertThat(job.source()).contains("synthetic fixture");
            assertThat(job.sourceUrl()).startsWith(
                    "https://fixtures.jobseekercopilot.test/");
            assertThat(job.applicationUrl()).isEqualTo(job.sourceUrl());
        });
        assertThat(first.attribution().label()).isEqualTo("Vacancy source: NHS Jobs");
        assertThat(first.attribution().disclaimer()).contains("does not endorse");
    }

    @Test
    void supportsKeywordLocationAndPaginationWithoutExternalCalls() {
        var response = client.search(request("data", "remote"));

        assertThat(response.totalResults()).isEqualTo(1);
        assertThat(response.jobs()).extracting(job -> job.title())
                .containsExactly("Data Analyst");
    }

    private NhsJobsSearchRequest request(String keyword, String location) {
        return new NhsJobsSearchRequest(
                keyword,
                location,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                20);
    }
}
