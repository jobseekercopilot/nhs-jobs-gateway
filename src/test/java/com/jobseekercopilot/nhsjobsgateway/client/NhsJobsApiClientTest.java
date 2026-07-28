package com.jobseekercopilot.nhsjobsgateway.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jobseekercopilot.nhsjobsgateway.config.NhsJobsProperties;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class NhsJobsApiClientTest {
    private final AtomicInteger responseStatus = new AtomicInteger(200);
    private final AtomicLong responseDelayMillis = new AtomicLong();
    private final AtomicReference<URI> requestedUri = new AtomicReference<>();
    private HttpServer server;
    private NhsJobsProperties properties;

    @BeforeEach
    void startProviderStub() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/search_xml", this::respond);
        server.start();
        properties = new NhsJobsProperties();
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setReadTimeout(Duration.ofSeconds(2));
        properties.setConnectTimeout(Duration.ofSeconds(2));
    }

    @AfterEach
    void stopProviderStub() {
        server.stop(0);
    }

    @Test
    void usesTheOfficialV107FiltersAndNeverExposesInternalAdvertControls() {
        var response = new NhsJobsApiClient(properties).search(request());

        assertThat(response.jobs()).singleElement()
                .extracting(job -> job.externalJobId())
                .isEqualTo("C123");
        String query = requestedUri.get().getRawQuery();
        assertThat(query)
                .contains("keyword=Platform+Engineer")
                .contains("location=Leeds")
                .contains("distance=15")
                .contains("countryCode=ENG")
                .contains("contractType=Permanent")
                .contains("staffGroup=Administrative+%26+Clerical")
                .contains("workingPattern=Full+time")
                .contains("payBand=Band+6")
                .contains("salaryFrom=35000")
                .contains("salaryTo=50000")
                .contains("page=2")
                .contains("limit=25")
                .doesNotContain("employerCode")
                .doesNotContain("internalOnly")
                .doesNotContain("externalOnly");
    }

    @Test
    void translatesRateLimitingToAStableProviderStatus() {
        responseStatus.set(429);

        assertThatThrownBy(() -> new NhsJobsApiClient(properties).search(request()))
                .isInstanceOfSatisfying(
                        NhsJobsProviderException.class,
                        exception -> assertThat(exception.getStatus())
                                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }

    @Test
    void rejectsResponsesLargerThanTheConfiguredBound() {
        properties.setMaxResponseBytes(20);

        assertThatThrownBy(() -> new NhsJobsApiClient(properties).search(request()))
                .isInstanceOf(NhsJobsProviderException.class)
                .hasMessage("NHS Jobs response exceeded the configured size limit");
    }

    @Test
    void translatesAProviderTimeoutToStableUnavailableStatus() {
        properties.setReadTimeout(Duration.ofMillis(50));
        responseDelayMillis.set(250);

        assertThatThrownBy(() -> new NhsJobsApiClient(properties).search(request()))
                .isInstanceOfSatisfying(
                        NhsJobsProviderException.class,
                        exception -> {
                            assertThat(exception.getStatus())
                                    .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                            assertThat(exception.getMessage())
                                    .isEqualTo("NHS Jobs API request failed");
                        });
    }

    @Test
    void translatesAnUnavailableProviderToStableUnavailableStatus() {
        responseStatus.set(503);

        assertThatThrownBy(() -> new NhsJobsApiClient(properties).search(request()))
                .isInstanceOfSatisfying(
                        NhsJobsProviderException.class,
                        exception -> assertThat(exception.getStatus())
                                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    private NhsJobsSearchRequest request() {
        return new NhsJobsSearchRequest(
                "Platform Engineer",
                "Leeds",
                15,
                "ENG",
                List.of("Permanent"),
                List.of("Administrative & Clerical"),
                List.of("Full time"),
                List.of("Band 6"),
                35000,
                50000,
                "2026-07-01",
                "publicationDateDesc",
                2,
                25);
    }

    private void respond(HttpExchange exchange) throws IOException {
        requestedUri.set(exchange.getRequestURI());
        try {
            Thread.sleep(responseDelayMillis.get());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            exchange.close();
            return;
        }
        byte[] body = """
                <nhsJobs>
                  <totalPages>1</totalPages>
                  <totalResults>1</totalResults>
                  <vacancyDetails>
                    <id>C123</id>
                    <title>Platform Engineer</title>
                    <employer>Example NHS Trust</employer>
                    <locations><locations>Leeds</locations></locations>
                    <url>https://www.jobs.nhs.uk/candidate/jobadvert/C123</url>
                  </vacancyDetails>
                </nhsJobs>
                """.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/xml");
        exchange.sendResponseHeaders(responseStatus.get(), body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
