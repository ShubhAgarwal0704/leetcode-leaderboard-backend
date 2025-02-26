package com.project.leetcodedashboard.controllers;

import com.project.leetcodedashboard.entities.StatsResponse;
import com.project.leetcodedashboard.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v0")
@Tag(name = "Leetcode APIs")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping(value ={"/{username}", "/"})
    public StatsResponse getStats(@PathVariable Optional<String> username) {
        if (username.isPresent()) {
            return userService.getStats(username.get());
        } else {
            String status = "error";
            String msg = "please enter your username";
            return StatsResponse.error(status, msg);
        }
    }

    @GetMapping("/leaderboard")
    public List<StatsResponse> getLeaderboard() {
        if(userService.getLeaderboard().isPresent()){
            return userService.getLeaderboard().get();
        }else{
            List<StatsResponse> statsResponseList = new ArrayList<>();
            statsResponseList.add(StatsResponse.error("error", "some error occurred while fetching leaderboard"));
            return statsResponseList;
        }
    }

    @GetMapping("/default")
    public List<StatsResponse> getDefaulters() {
        if(userService.getDefault().isPresent()){
            return userService.getDefault().get();
        }else{
            List<StatsResponse> statsResponseList = new ArrayList<>();
            statsResponseList.add(StatsResponse.error("error", "No Defaulters found"));
            return statsResponseList;
        }
    }
}

