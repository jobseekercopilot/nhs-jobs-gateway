package com.jobseekercopilot.nhsjobsgateway.client;

import com.jobseekercopilot.nhsjobsgateway.config.NhsJobsProperties;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchRequest;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "external-provider", name = "mode", havingValue = "LIVE")
public class NhsJobsApiClient implements NhsJobsProviderClient {
    private static final Logger log = LoggerFactory.getLogger(NhsJobsApiClient.class);
    private final NhsJobsProperties properties;
    private final HttpClient httpClient;
    private final NhsJobsXmlParser parser = new NhsJobsXmlParser();

    public NhsJobsApiClient(NhsJobsProperties properties) {
        this(
                properties,
                HttpClient.newBuilder()
                        .connectTimeout(properties.getConnectTimeout())
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .build());
    }

    NhsJobsApiClient(NhsJobsProperties properties, HttpClient httpClient) {
        this.properties = properties;
        this.httpClient = httpClient;
    }

    @Override
    public NhsJobsSearchResponse search(NhsJobsSearchRequest request) {
        int page = request.resolvedPage();
        int limit = request.resolvedResultsPerPage(properties.getResultsPerPage());
        if (!properties.isEnabled()) {
            log.warn("NHS Jobs provider is disabled");
            return NhsJobsSearchResponse.disabled(page, limit);
        }

        long startedAt = System.nanoTime();
        try {
            URI uri = searchUri(request, page, limit);
            HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                    .timeout(properties.getReadTimeout())
                    .header("Accept", "application/xml, text/xml")
                    .GET()
                    .build();
            HttpResponse<InputStream> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 429) {
                close(response.body());
                throw new NhsJobsProviderException(
                        "NHS Jobs rate limit reached",
                        HttpStatus.TOO_MANY_REQUESTS);
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                close(response.body());
                throw new NhsJobsProviderException("NHS Jobs API request failed");
            }
            byte[] body = readBounded(response.body(), properties.getMaxResponseBytes());
            NhsJobsSearchResponse mapped = parser.parse(body, page, limit);
            log.info(
                    "NHS Jobs provider returned status={} resultCount={} totalResults={} durationMs={}",
                    response.statusCode(),
                    mapped.jobs().size(),
                    mapped.totalResults(),
                    elapsedMillis(startedAt));
            return mapped;
        } catch (NhsJobsProviderException exception) {
            log.warn(
                    "NHS Jobs provider failed status={} durationMs={}",
                    exception.getStatus().value(),
                    elapsedMillis(startedAt));
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw unavailable(startedAt, "interrupted");
        } catch (IOException | IllegalArgumentException exception) {
            throw unavailable(startedAt, exception.getClass().getSimpleName());
        }
    }

    URI searchUri(NhsJobsSearchRequest request, int page, int limit) {
        List<String> query = new ArrayList<>();
        add(query, "keyword", request.keyword());
        add(query, "location", request.location());
        add(query, "distance", request.distanceMiles());
        add(query, "countryCode", request.countryCode());
        addAll(query, "contractType", request.contractTypes());
        addAll(query, "staffGroup", request.staffGroups());
        addAll(query, "workingPattern", request.workingPatterns());
        addAll(query, "payBand", request.payBands());
        add(query, "salaryFrom", request.salaryFrom());
        add(query, "salaryTo", request.salaryTo());
        add(query, "publishedFrom", request.publishedFrom());
        add(query, "sort", request.sort());
        add(query, "page", page);
        add(query, "limit", limit);
        String base = properties.getBaseUrl().replaceAll("/+$", "");
        String path = properties.getSearchPath().startsWith("/")
                ? properties.getSearchPath()
                : "/" + properties.getSearchPath();
        return URI.create(base + path + "?" + String.join("&", query));
    }

    private byte[] readBounded(InputStream input, int maximumBytes) throws IOException {
        try (input) {
            byte[] result = input.readNBytes(maximumBytes + 1);
            if (result.length > maximumBytes) {
                throw new NhsJobsProviderException("NHS Jobs response exceeded the configured size limit");
            }
            return result;
        }
    }

    private void close(InputStream input) {
        try {
            input.close();
        } catch (IOException ignored) {
            // No response body is consumed for upstream errors.
        }
    }

    private void addAll(List<String> query, String name, List<String> values) {
        if (values != null) {
            values.forEach(value -> add(query, name, value));
        }
    }

    private void add(List<String> query, String name, Object value) {
        if (value != null && !value.toString().isBlank()) {
            query.add(encode(name) + "=" + encode(value.toString()));
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private NhsJobsProviderException unavailable(long startedAt, String errorType) {
        log.warn(
                "NHS Jobs provider failed durationMs={} error={}",
                elapsedMillis(startedAt),
                errorType);
        return new NhsJobsProviderException("NHS Jobs API request failed");
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
