package com.jobseekercopilot.nhsjobsgateway.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class NhsJobsXmlParserTest {
    private final NhsJobsXmlParser parser = new NhsJobsXmlParser();

    @Test
    void mapsThePublishedSelfServeXmlShape() {
        String xml = """
                <vacancies>
                  <totalResults>1</totalResults><totalPages>1</totalPages>
                  <vacancyDetails>
                    <id>123</id><reference>C123</reference><title>Staff Nurse</title>
                    <employer>Example NHS Trust</employer><description>Safe patient care</description>
                    <locations><location>Leeds, LS1 1AA</location><location>Bradford, BD1 1AA</location></locations>
                    <salary>£30,000 to £36,000 a year</salary><type>Permanent</type>
                    <postDate>2026-08-01T09:00:00Z</postDate><closeDate>2026-08-31</closeDate>
                    <url>https://www.jobs.nhs.uk/candidate/jobadvert/C123</url>
                  </vacancyDetails>
                </vacancies>
                """;

        var response = parser.parse(xml, 1, 50);

        assertThat(response.totalAvailable()).isEqualTo(1);
        assertThat(response.jobs()).singleElement().satisfies(job -> {
            assertThat(job.externalJobId()).isEqualTo("123");
            assertThat(job.locations()).containsExactly("Leeds, LS1 1AA", "Bradford, BD1 1AA");
            assertThat(job.salaryMinimum()).isEqualByComparingTo(new BigDecimal("30000"));
            assertThat(job.salaryMaximum()).isEqualByComparingTo(new BigDecimal("36000"));
            assertThat(job.salaryPeriod()).isEqualTo("YEAR");
        });
    }

    @Test
    void rejectsDocumentTypeDeclarations() {
        String xml = "<!DOCTYPE foo [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]><vacancies><totalResults>&xxe;</totalResults></vacancies>";
        assertThatThrownBy(() -> parser.parse(xml, 1, 10))
                .isInstanceOf(NhsJobsProviderException.class)
                .hasMessage("NHS Jobs returned malformed XML");
    }
}
