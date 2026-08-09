package com.jobseekercopilot.nhsjobsgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NhsJobsGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(NhsJobsGatewayApplication.class, args);
    }
}
