package com.jobseekercopilot.nhsjobsgateway.controller;

import com.jobseekercopilot.nhsjobsgateway.config.ExternalProviderProperties;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class ProviderModeController {
    private final ExternalProviderProperties properties;
    public ProviderModeController(ExternalProviderProperties properties) { this.properties = properties; }
    @GetMapping("/provider-mode") public Map<String, Object> providerMode() {
        return Map.of("gateway", "nhs-jobs-gateway", "mode", properties.getMode().name(),
                "externalCallsEnabled", properties.getMode().name().equals("LIVE"), "credentialsRequired", false);
    }
}
