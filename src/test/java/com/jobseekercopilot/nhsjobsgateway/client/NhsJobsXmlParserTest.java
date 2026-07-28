package com.jobseekercopilot.nhsjobsgateway.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class NhsJobsXmlParserTest {
    private final NhsJobsXmlParser parser = new NhsJobsXmlParser();

    @Test
    void mapsThePinnedV107ResponseShapeAndSafeLinks() {
        String xml = """
                <nhsJobs>
                  <totalPages>3</totalPages>
                  <totalResults>21</totalResults>
                  <vacancyDetails>
                    <id>C123-FIXTURE</id>
                    <title>Staff Nurse</title>
                    <employer>Example NHS Trust</employer>
                    <description>A deliberately synthetic overview.</description>
                    <closeDate>2026-08-20</closeDate>
                    <postDate>2026-07-20</postDate>
                    <locations>
                      <locations>London, SW1A 1AA</locations>
                      <locations>Remote</locations>
                    </locations>
                    <reference>123-FIXTURE</reference>
                    <salary>£31,049 to £37,796 a year</salary>
                    <type>Permanent</type>
                    <url>https://www.jobs.nhs.uk/candidate/jobadvert/C123-FIXTURE</url>
                  </vacancyDetails>
                </nhsJobs>
                """;

        var response = parser.parse(xml.getBytes(StandardCharsets.UTF_8), 2, 10);

        assertThat(response.provider()).isEqualTo("NHS_JOBS");
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.totalResults()).isEqualTo(21);
        assertThat(response.jobs()).singleElement().satisfies(job -> {
            assertThat(job.externalJobId()).isEqualTo("C123-FIXTURE");
            assertThat(job.reference()).isEqualTo("123-FIXTURE");
            assertThat(job.title()).isEqualTo("Staff Nurse");
            assertThat(job.employer()).isEqualTo("Example NHS Trust");
            assertThat(job.locations()).containsExactly("London, SW1A 1AA", "Remote");
            assertThat(job.salaryText()).isEqualTo("£31,049 to £37,796 a year");
            assertThat(job.sourceUrl()).isEqualTo(
                    "https://www.jobs.nhs.uk/candidate/jobadvert/C123-FIXTURE");
            assertThat(job.applicationUrl()).isEqualTo(job.sourceUrl());
        });
    }

    @Test
    void removesAProviderLinkOutsideTheNhsAllowlist() {
        String xml = """
                <nhsJobs>
                  <totalPages>1</totalPages>
                  <totalResults>1</totalResults>
                  <vacancyDetails>
                    <id>C123</id>
                    <title>Role</title>
                    <employer>Trust</employer>
                    <locations><locations>London</locations></locations>
                    <url>https://evil.example/capture</url>
                  </vacancyDetails>
                </nhsJobs>
                """;

        var job = parser.parse(xml.getBytes(StandardCharsets.UTF_8), 1, 50)
                .jobs()
                .get(0);

        assertThat(job.sourceUrl()).isNull();
        assertThat(job.applicationUrl()).isNull();
    }

    @Test
    void rejectsDoctypesAndExternalEntities() {
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE nhsJobs [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <nhsJobs>
                  <totalPages>1</totalPages>
                  <totalResults>1</totalResults>
                  <vacancyDetails><id>&xxe;</id></vacancyDetails>
                </nhsJobs>
                """;

        assertThatThrownBy(() -> parser.parse(
                xml.getBytes(StandardCharsets.UTF_8),
                1,
                50))
                .isInstanceOf(NhsJobsProviderException.class)
                .hasMessage("NHS Jobs response could not be parsed");
    }
}
