package com.project.leetcodedashboard;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Leetcode Leaderboard", description = "by Shubh Agarwal"))
public class LeetcodeDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeetcodeDashboardApplication.class, args);
    }

}
