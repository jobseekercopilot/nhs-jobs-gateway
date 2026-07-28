package com.jobseekercopilot.nhsjobsgateway.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

class ProviderModeSafetyTest {
    @Test
    void liveModeAllowsOnlyTheOfficialNhsJobsOrigin() {
        ExternalProviderProperties provider = new ExternalProviderProperties();
        provider.setMode(ExternalProviderMode.LIVE);
        NhsJobsProperties nhsJobs = new NhsJobsProperties();
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);

        assertThatCode(() -> new ProviderModeSafety(
                provider,
                nhsJobs,
                new FixtureProperties(),
                environment).run(null)).doesNotThrowAnyException();

        nhsJobs.setBaseUrl("https://www.jobs.nhs.uk.evil.example");
        assertThatThrownBy(() -> new ProviderModeSafety(
                provider,
                nhsJobs,
                new FixtureProperties(),
                environment).run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("official");
    }

    @Test
    void liveModeAllowsOnlyTheDocumentedXmlSearchPath() {
        ExternalProviderProperties provider = new ExternalProviderProperties();
        provider.setMode(ExternalProviderMode.LIVE);
        NhsJobsProperties nhsJobs = new NhsJobsProperties();
        nhsJobs.setSearchPath("/api/v1/search_rss");

        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        assertThatThrownBy(() -> new ProviderModeSafety(
                provider, nhsJobs, new FixtureProperties(), environment).run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("/api/v1/search_xml");
    }

    @Test
    void fixtureModeFailsClosedWithAProductionProfile() {
        ExternalProviderProperties provider = new ExternalProviderProperties();
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"production"});

        assertThatThrownBy(() -> new ProviderModeSafety(
                provider,
                new NhsJobsProperties(),
                new FixtureProperties(),
                environment).run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot start in FIXTURE mode");
    }
}
