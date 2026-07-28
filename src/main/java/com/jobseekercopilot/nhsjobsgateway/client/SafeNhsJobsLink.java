package com.jobseekercopilot.nhsjobsgateway.client;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

final class SafeNhsJobsLink {
    private static final String LIVE_HOST = "www.jobs.nhs.uk";
    private static final String FEED_HOST = "beta.jobs.nhs.uk";
    private static final String FIXTURE_HOST = "fixtures.jobseekercopilot.test";
    private static final String ADVERT_PATH = "/candidate/jobadvert/";

    private SafeNhsJobsLink() {
    }

    static Optional<String> live(
            String candidate,
            String externalJobId,
            String reference) {
        Optional<String> approved = allowlisted(candidate, LIVE_HOST);
        if (approved.isEmpty()) {
            approved = allowlisted(candidate, FEED_HOST);
        }
        return approved
                .map(URI::create)
                .filter(uri -> matchesIdentity(uri, externalJobId, reference))
                .map(uri -> "https://" + LIVE_HOST + uri.getRawPath());
    }

    private static boolean matchesIdentity(
            URI uri,
            String externalJobId,
            String reference) {
        String path = uri.getRawPath();
        if (path == null || !path.startsWith(ADVERT_PATH)) {
            return false;
        }
        String pathIdentifier = path.substring(ADVERT_PATH.length());
        if (!pathIdentifier.matches("[A-Za-z0-9-]{1,255}")) {
            return false;
        }
        return pathIdentifier.equals(externalJobId)
                || pathIdentifier.equals(reference);
    }

    static String fixture(String id) {
        if (id == null || !id.matches("[A-Za-z0-9-]{1,255}")) {
            throw new IllegalStateException(
                    "Invalid synthetic fixture identifier");
        }
        String candidate =
                "https://" + FIXTURE_HOST + "/nhs-jobs/jobadvert/" + id;
        return allowlisted(candidate, FIXTURE_HOST)
                .orElseThrow(() -> new IllegalStateException(
                        "Invalid synthetic fixture link"));
    }

    private static Optional<String> allowlisted(
            String candidate,
            String expectedHost) {
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
                    && uri.getPort() == -1
                    && uri.getFragment() == null
                    && uri.getRawQuery() == null;
            return safe ? Optional.of(uri.normalize().toASCIIString())
                    : Optional.empty();
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
