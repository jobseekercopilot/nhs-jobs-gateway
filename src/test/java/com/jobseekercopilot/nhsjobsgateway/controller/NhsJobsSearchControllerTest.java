package com.jobseekercopilot.nhsjobsgateway.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jobseekercopilot.nhsjobsgateway.client.NhsJobsProviderClient;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NhsJobsSearchController.class)
class NhsJobsSearchControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private NhsJobsProviderClient providerClient;

    @Test
    void exposesTheStableProviderContract() throws Exception {
        when(providerClient.search(any())).thenReturn(
                NhsJobsSearchResponse.available(0, 0, 1, 20, List.of()));

        mvc.perform(post("/api/v1/nhs/jobs/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyword\":\"nurse\",\"page\":1,\"resultsPerPage\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("NHS_JOBS"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.attribution.label").value("Vacancy source: NHS Jobs"))
                .andExpect(jsonPath("$.jobs").isArray());
    }

    @Test
    void rejectsProviderLimitsAboveOneHundred() throws Exception {
        mvc.perform(post("/api/v1/nhs/jobs/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resultsPerPage\":101}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnsupportedNhsJobsCountryCodes() throws Exception {
        mvc.perform(post("/api/v1/nhs/jobs/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"location":"London","distanceMiles":25,
                                 "countryCode":"ENG"}"""))
                .andExpect(status().isBadRequest());
    }
}
