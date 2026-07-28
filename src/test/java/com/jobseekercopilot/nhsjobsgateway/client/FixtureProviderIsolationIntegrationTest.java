package com.jobseekercopilot.nhsjobsgateway.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchRequest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "external-provider.mode=FIXTURE",
            "nhs-jobs.base-url=https://egress-must-not-be-reached.invalid"
        })
class FixtureProviderIsolationIntegrationTest {
    @Autowired
    private ApplicationContext context;

    @Autowired
    private NhsJobsProviderClient providerClient;

    @Test
    void fixtureModeHasNoLiveClientAndCompletesWithZeroExternalEgress() {
        Map<String, NhsJobsProviderClient> clients =
                context.getBeansOfType(NhsJobsProviderClient.class);

        assertThat(clients).hasSize(1);
        assertThat(providerClient).isExactlyInstanceOf(FixtureNhsJobsProviderClient.class);
        assertThat(context.getBeansOfType(NhsJobsApiClient.class)).isEmpty();

        var response = providerClient.search(new NhsJobsSearchRequest(
                "nurse",
                null,
                null, null, null, null, null, null, null, null, null, null,
                1,
                20));

        assertThat(response.status()).isEqualTo("AVAILABLE");
        assertThat(response.jobs()).singleElement();
    }
}
