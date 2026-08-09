package com.jobseekercopilot.nhsjobsgateway.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;

class ProviderModeSafetyTest {
    @Test
    void productionCannotRunWithFixtureData() {
        var external = new ExternalProviderProperties();
        external.setMode(ExternalProviderMode.FIXTURE);
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"production"});

        assertThatThrownBy(() -> new ProviderModeSafety(external, environment).run(mock(ApplicationArguments.class)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot start in FIXTURE mode");
    }
}
