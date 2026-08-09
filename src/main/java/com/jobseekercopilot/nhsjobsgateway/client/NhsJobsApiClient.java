package com.jobseekercopilot.nhsjobsgateway.client;

import com.jobseekercopilot.nhsjobsgateway.config.NhsJobsProperties;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchRequest;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchResponse;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@ConditionalOnProperty(prefix = "external-provider", name = "mode", havingValue = "LIVE")
public class NhsJobsApiClient implements NhsJobsProviderClient {
    private static final Logger log = LoggerFactory.getLogger(NhsJobsApiClient.class);
    private final NhsJobsProperties properties;
    private final NhsJobsXmlParser parser;
    private final WebClient webClient;
    public NhsJobsApiClient(NhsJobsProperties properties, NhsJobsXmlParser parser) {
        this.properties = properties;
        this.parser = parser;
        this.webClient = WebClient.builder().baseUrl(properties.getBaseUrl()).build();
    }
    @Override public NhsJobsSearchResponse search(NhsJobsSearchRequest request) {
        int page = request.page() == null ? 1 : Math.max(1, request.page());
        int pageSize = request.resultsPerPage() == null ? properties.getResultsPerPage() : Math.max(1, Math.min(100, request.resultsPerPage()));
        if (!properties.isEnabled()) return new NhsJobsSearchResponse(0, 0, page, pageSize, java.util.List.of());
        long started = System.nanoTime();
        try {
            String body = webClient.get().uri(builder -> {
                if (text(request.targetRole())) builder.queryParam("keyword", request.targetRole());
                if (text(request.location())) {
                    builder.queryParam("location", request.location());
                    builder.queryParam("distance", request.distanceMiles() == null ? 25 : request.distanceMiles());
                    builder.queryParam("countryCode", text(request.countryCode()) ? request.countryCode() : properties.getDefaultCountryCode());
                }
                if (request.contractTypes() != null && !request.contractTypes().isEmpty()) builder.queryParam("contractType", String.join(",", request.contractTypes()));
                if (request.workingPatterns() != null && !request.workingPatterns().isEmpty()) builder.queryParam("workingPattern", String.join(",", request.workingPatterns()));
                if (request.salaryMinimum() != null) builder.queryParam("salaryFrom", request.salaryMinimum());
                if (request.salaryMaximum() != null) builder.queryParam("salaryTo", request.salaryMaximum());
                if (request.postedWithinDays() != null && request.postedWithinDays() > 0) builder.queryParam("publishedFrom", LocalDate.now().minusDays(request.postedWithinDays()));
                return builder.queryParam("page", page).queryParam("limit", pageSize).queryParam("sort", "publicationDateDesc").build();
            }).retrieve().bodyToMono(String.class).block();
            if (body == null || body.isBlank()) throw new NhsJobsProviderException("NHS Jobs returned an empty response", null);
            NhsJobsSearchResponse response = parser.parse(body, page, pageSize);
            log.info("NHS Jobs returned resultCount={} totalAvailable={} durationMs={}", response.jobs().size(), response.totalAvailable(), (System.nanoTime() - started) / 1_000_000);
            return response;
        } catch (WebClientResponseException.TooManyRequests exception) {
            throw new NhsJobsProviderException("NHS Jobs rate limit reached", null, HttpStatus.TOO_MANY_REQUESTS);
        } catch (WebClientResponseException exception) {
            throw new NhsJobsProviderException("NHS Jobs request failed", null,
                    exception.getStatusCode() == HttpStatus.UNAUTHORIZED || exception.getStatusCode() == HttpStatus.FORBIDDEN
                            ? HttpStatus.valueOf(exception.getStatusCode().value()) : HttpStatus.SERVICE_UNAVAILABLE);
        } catch (NhsJobsProviderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new NhsJobsProviderException("NHS Jobs request failed", null);
        }
    }
    private boolean text(String value) { return value != null && !value.isBlank(); }
}
