package com.jobseekercopilot.nhsjobsgateway.client;

import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchRequest;
import com.jobseekercopilot.nhsjobsgateway.model.NhsJobsSearchResponse;

public interface NhsJobsProviderClient {
    NhsJobsSearchResponse search(NhsJobsSearchRequest request);
}
