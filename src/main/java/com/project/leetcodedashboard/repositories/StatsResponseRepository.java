package com.project.leetcodedashboard.repositories;

import com.project.leetcodedashboard.entities.StatsResponse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatsResponseRepository extends MongoRepository<StatsResponse, String> {

    Optional<StatsResponse> findByLeetcodeUrl(String leetcodeUrl);

    List<StatsResponse> findAllByOrderByRankingAsc();
}
