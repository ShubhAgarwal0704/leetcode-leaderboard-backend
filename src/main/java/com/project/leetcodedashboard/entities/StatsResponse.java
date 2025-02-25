package com.project.leetcodedashboard.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

@Document(collection = "stats_response")
@Getter
@Setter
@NoArgsConstructor
public class StatsResponse {
    @Id
    private  String leetcodeUrl;
    private  String status;
    private  String message;
    private  int totalSolved;
    private  int totalQuestions;
    private  int easySolved;
    private  int totalEasy;
    private  int mediumSolved;
    private  int totalMedium;
    private  int hardSolved;
    private  int totalHard;
    private  float acceptanceRate;
    private  int ranking;
    private  int contributionPoints;
    private  int reputation;
    private  Map<LocalDate, Integer> submissionCalendar;

    public StatsResponse(String status, String message, String leetcodeUrl, int totalSolved, int totalQuestions, int easySolved, int totalEasy, int mediumSolved, int totalMedium, int hardSolved, int totalHard, float acceptanceRate, int ranking, int contributionPoints, int reputation, Map<LocalDate, Integer> submissionCalendar) {
        this.status = status;
        this.message = message;
        this.leetcodeUrl = leetcodeUrl;
        this.totalSolved = totalSolved;
        this.totalQuestions = totalQuestions;
        this.easySolved = easySolved;
        this.totalEasy = totalEasy;
        this.mediumSolved = mediumSolved;
        this.totalMedium = totalMedium;
        this.hardSolved = hardSolved;
        this.totalHard = totalHard;
        this.acceptanceRate = acceptanceRate;
        this.ranking = ranking;
        this.contributionPoints = contributionPoints;
        this.reputation = reputation;
        this.submissionCalendar = submissionCalendar;
    }


    public static StatsResponse error(String status, String message) {
        return new StatsResponse(status, message, "https://leetcode.com/", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Collections.emptyMap());
    }
}
