package com.jobseekercopilot.nhsjobsgateway.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobseekercopilot.nhsjobsgateway.config.NhsJobsProperties;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJob;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchRequest;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class NhsJobsApiClientTest {

    @Test
    void preservesTheKeywordAndLetsNhsUseItsDefaultRelevanceOrdering() throws IOException {
        AtomicReference<URI> requestedUri = new AtomicReference<>();
        HttpServer server = HttpServer.create(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/search_xml", exchange -> {
            requestedUri.set(exchange.getRequestURI());
            byte[] body = """
                    <nhsJobs>
                      <vacancyDetails>
                        <id>software-1</id>
                        <reference>SOFTWARE-1</reference>
                        <title>Lead Software Developer</title>
                        <employer>Example NHS Trust</employer>
                        <url>https://www.jobs.nhs.uk/candidate/jobadvert/SOFTWARE-1</url>
                      </vacancyDetails>
                      <totalPages>1</totalPages>
                      <totalResults>1</totalResults>
                    </nhsJobs>
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();

        try {
            NhsJobsProperties properties = new NhsJobsProperties();
            properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort()
                    + "/search_xml");
            NhsJobsApiClient client = new NhsJobsApiClient(
                    properties, new NhsJobsXmlParser());

            var response = client.search(new NhsJobsSearchRequest(
                    "software developer",
                    null,
                    null,
                    null,
                    List.of(),
                    List.of(),
                    null,
                    null,
                    null,
                    1,
                    10));

            assertThat(response.jobs())
                    .extracting(NhsJob::title)
                    .containsExactly("Lead Software Developer");
            String query = URLDecoder.decode(
                    requestedUri.get().getRawQuery(), StandardCharsets.UTF_8);
            assertThat(query)
                    .contains("keyword=software developer", "page=1", "limit=10")
                    .doesNotContain("sort=");
        } finally {
            server.stop(0);
        }
    }
}
