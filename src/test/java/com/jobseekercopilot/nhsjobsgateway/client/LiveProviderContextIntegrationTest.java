package com.jobseekercopilot.nhsjobsgateway.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "external-provider.mode=LIVE",
            "nhs-jobs.enabled=true"
        })
class LiveProviderContextIntegrationTest {
    @Autowired
    private ApplicationContext context;

    @Autowired
    private NhsJobsProviderClient providerClient;

    @Test
    void liveModeStartsWithExactlyOneLiveProviderClient() {
        Map<String, NhsJobsProviderClient> clients =
                context.getBeansOfType(NhsJobsProviderClient.class);

        assertThat(clients).hasSize(1);
        assertThat(providerClient).isExactlyInstanceOf(NhsJobsApiClient.class);
        assertThat(context.getBeansOfType(FixtureNhsJobsProviderClient.class)).isEmpty();
    }
}
