package com.jobseekercopilot.nhsjobsgateway.client;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

final class SafeNhsJobsLink {
    private static final String LIVE_HOST = "www.jobs.nhs.uk";
    private static final String FIXTURE_HOST = "fixtures.jobseekercopilot.test";

    private SafeNhsJobsLink() {
    }

    static Optional<String> live(String candidate) {
        return allowlisted(candidate, LIVE_HOST)
                .filter(link -> URI.create(link).getPath()
                        .startsWith("/candidate/jobadvert/"));
    }

    static String fixture(String id) {
        String candidate = "https://" + FIXTURE_HOST + "/nhs-jobs/jobadvert/" + id;
        return allowlisted(candidate, FIXTURE_HOST)
                .orElseThrow(() -> new IllegalStateException("Invalid synthetic fixture link"));
    }

    private static Optional<String> allowlisted(String candidate, String expectedHost) {
        if (candidate == null || candidate.isBlank()) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(candidate.trim());
            String host = uri.getHost();
            boolean safe = "https".equalsIgnoreCase(uri.getScheme())
                    && host != null
                    && expectedHost.equals(host.toLowerCase(Locale.ROOT))
                    && uri.getUserInfo() == null
                    && (uri.getPort() == -1 || uri.getPort() == 443)
                    && uri.getFragment() == null;
            return safe ? Optional.of(uri.normalize().toASCIIString()) : Optional.empty();
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
