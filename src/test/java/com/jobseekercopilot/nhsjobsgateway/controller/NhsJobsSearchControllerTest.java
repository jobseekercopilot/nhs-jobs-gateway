package com.jobseekercopilot.nhsjobsgateway.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "external-provider.mode=FIXTURE")
@AutoConfigureMockMvc
class NhsJobsSearchControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void returnsAStableFixtureWithoutAnExternalCall() throws Exception {
        mvc.perform(post("/api/v1/nhs-jobs/jobs/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetRole\":\"nurse\",\"page\":1,\"resultsPerPage\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("NHS_JOBS"))
                .andExpect(jsonPath("$.jobs[0].externalJobId").value("nhs-fixture-1"))
                .andExpect(jsonPath("$.jobs[0].sourceUrl").value("https://www.jobs.nhs.uk/candidate/jobadvert/NHS-FIXTURE-1"));
    }

    @Test
    void exposesTruthfulFixtureMode() throws Exception {
        mvc.perform(get("/internal/provider-mode"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("FIXTURE"))
                .andExpect(jsonPath("$.externalCallsEnabled").value(false));
    }
}
