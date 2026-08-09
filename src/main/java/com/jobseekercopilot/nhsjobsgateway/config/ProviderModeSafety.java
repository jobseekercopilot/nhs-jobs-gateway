package com.jobseekercopilot.nhsjobsgateway.config;

import java.util.Arrays;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProviderModeSafety implements ApplicationRunner {
    private final ExternalProviderProperties external;
    private final Environment environment;
    public ProviderModeSafety(ExternalProviderProperties external, Environment environment) {
        this.external = external;
        this.environment = environment;
    }
    @Override public void run(ApplicationArguments args) {
        boolean production = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(value -> value.equalsIgnoreCase("prod") || value.equalsIgnoreCase("production"));
        if (production && external.getMode() == ExternalProviderMode.FIXTURE) {
            throw new IllegalStateException("nhs-jobs-gateway cannot start in FIXTURE mode with a production profile");
        }
    }
}
