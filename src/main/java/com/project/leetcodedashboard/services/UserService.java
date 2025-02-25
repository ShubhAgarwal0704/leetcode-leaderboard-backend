package com.project.leetcodedashboard.services;

import com.project.leetcodedashboard.entities.StatsResponse;
import com.project.leetcodedashboard.repositories.StatsResponseRepository;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class UserService{


    @Autowired
    StatsResponseRepository statsResponseRepository;

    public StatsResponse getStats(String username) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        String query = String.format("{\"query\":\"query getUserProfile($username: String!) { allQuestionsCount { difficulty count } matchedUser(username: $username) { contributions { points } profile { reputation ranking } submissionCalendar submitStats { acSubmissionNum { difficulty count submissions } totalSubmissionNum { difficulty count submissions } } } } \",\"variables\":{\"username\":\"%s\"}}", username);
        RequestBody body = RequestBody.create(mediaType, query);
        Request request = new Request.Builder()
                .url("https://leetcode.com/graphql/")
                .method("POST", body)
                .addHeader("referer", String.format("https://leetcode.com/%s/", username))
                .addHeader("Content-Type", "application/json")
                .build();

        try {
            Response response = client.newCall(request).execute();

            // Inspect response
            String responseString = response.body().string();
            JSONObject jsonObject = new JSONObject(responseString);

            if (response.isSuccessful()) {
                // Parse GraphQL response

                // User not found
                if (jsonObject.has("errors")) {
                    return StatsResponse.error("error", "user does not exist");
                } else { // Parse user info
                    return decodeGraphqlJson(jsonObject, username);
                }
            } else {
                return StatsResponse.error("error", jsonObject.getString("error"));
            }
        } catch (IOException | JSONException ex) {
            return StatsResponse.error("error", ex.getMessage());
        }

    }

    private StatsResponse decodeGraphqlJson(JSONObject json, String username) {
        String leetcodeUrl = "https://leetcode.com/u";
        int totalSolved = 0;
        int totalQuestions = 0;
        int easySolved = 0;
        int totalEasy = 0;
        int mediumSolved = 0;
        int totalMedium = 0;
        int hardSolved = 0;
        int totalHard = 0;
        float acceptanceRate = 0;
        int ranking = 0;
        int contributionPoints = 0;
        int reputation = 0;

        final Map<String, Integer> submissionCalendar = new TreeMap<>();
        Map<LocalDate, Integer> readableSubmissionCalendar;

        try {
            JSONObject data = json.getJSONObject("data");
            JSONArray allQuestions = data.getJSONArray("allQuestionsCount");
            JSONObject matchedUser = data.getJSONObject("matchedUser");
            JSONObject submitStats = matchedUser.getJSONObject("submitStats");
            JSONArray actualSubmissions = submitStats.getJSONArray("acSubmissionNum");
            JSONArray totalSubmissions = submitStats.getJSONArray("totalSubmissionNum");

            // Fill in total counts
            totalQuestions = allQuestions.getJSONObject(0).getInt("count");
            totalEasy = allQuestions.getJSONObject(1).getInt("count");
            totalMedium = allQuestions.getJSONObject(2).getInt("count");
            totalHard = allQuestions.getJSONObject(3).getInt("count");

            // Fill in solved counts
            totalSolved = actualSubmissions.getJSONObject(0).getInt("count");
            easySolved = actualSubmissions.getJSONObject(1).getInt("count");
            mediumSolved = actualSubmissions.getJSONObject(2).getInt("count");
            hardSolved = actualSubmissions.getJSONObject(3).getInt("count");
            leetcodeUrl = String.format("%s/%s/", leetcodeUrl, username);

            // Fill in etc
            float totalAcceptCount = actualSubmissions.getJSONObject(0).getInt("submissions");
            float totalSubCount = totalSubmissions.getJSONObject(0).getInt("submissions");
            if (totalSubCount != 0) {
                acceptanceRate = round((totalAcceptCount / totalSubCount) * 100, 2);
            }

            contributionPoints = matchedUser.getJSONObject("contributions").getInt("points");
            reputation = matchedUser.getJSONObject("profile").getInt("reputation");
            ranking = matchedUser.getJSONObject("profile").getInt("ranking");

            final JSONObject submissionCalendarJson = new JSONObject(matchedUser.getString("submissionCalendar"));

            for (String timeKey: submissionCalendarJson.keySet()) {
                submissionCalendar.put(timeKey, submissionCalendarJson.getInt(timeKey));
            }

            readableSubmissionCalendar = convertToDateMap(submissionCalendar);

            saveOrUpdateRepository(new StatsResponse("success", "retrieved", leetcodeUrl, totalSolved, totalQuestions, easySolved, totalEasy, mediumSolved, totalMedium, hardSolved, totalHard, acceptanceRate, ranking, contributionPoints, reputation, readableSubmissionCalendar));
        } catch (JSONException ex) {
            return StatsResponse.error("error", ex.getMessage());
        }

        return new StatsResponse("success", "retrieved", leetcodeUrl, totalSolved, totalQuestions, easySolved, totalEasy, mediumSolved, totalMedium, hardSolved, totalHard, acceptanceRate, ranking, contributionPoints, reputation, readableSubmissionCalendar);
    }

    private Map<LocalDate, Integer> convertToDateMap(Map<String, Integer> submissionCalendar) {
        Map<LocalDate, Integer> readableDatesMap = new TreeMap<>((date1, date2) -> date2.compareTo(date1));

        for (Map.Entry<String, Integer> entry : submissionCalendar.entrySet()) {
            // Convert the Unix timestamp (in seconds) to a LocalDate
            long timestamp = Long.parseLong(entry.getKey());
            LocalDate date = Instant.ofEpochSecond(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            // Add the converted date and the corresponding value to the new map
            readableDatesMap.put(date, entry.getValue());
        }

        return readableDatesMap;
    }

    private StatsResponse saveOrUpdateRepository(StatsResponse statsResponse) {
        statsResponseRepository.findById(statsResponse.getLeetcodeUrl()).ifPresent(statsResponseRepository::delete);

        return statsResponseRepository.save(statsResponse);
    }

    private float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public Optional<List<StatsResponse>> getLeaderboard() {
        updateStatsOfAllUsers();
        List<StatsResponse> leaderboard = statsResponseRepository.findAllByOrderByRankingAsc();
        return leaderboard.isEmpty() ? Optional.empty() : Optional.of(leaderboard);
    }

    public Optional<List<StatsResponse>> getDefault() {
        updateStatsOfAllUsers();
        LocalDate today = LocalDate.now();
        List<StatsResponse> defaulters = statsResponseRepository.findAll().stream()
                .filter(user -> user.getSubmissionCalendar() == null || !user.getSubmissionCalendar().containsKey(today))
                .collect(Collectors.toList());

        return defaulters.isEmpty() ? Optional.empty() : Optional.of(defaulters);
    }

    private void updateStatsOfAllUsers() {
        List<StatsResponse> users = statsResponseRepository.findAll();

        for (StatsResponse statsResponse : users) {
            getStats(statsResponse.getLeetcodeUrl().replace("https://leetcode.com/u/", "").replace("/", ""));
        }
    }
}
