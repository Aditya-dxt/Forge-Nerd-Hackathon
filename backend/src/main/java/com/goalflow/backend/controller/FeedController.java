package com.goalflow.backend.controller;

import com.goalflow.backend.dto.ScoredContentItem;
import com.goalflow.backend.dto.UserGoal;
import com.goalflow.backend.repository.UserGoalRepository;
import com.goalflow.backend.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/feed")
public class FeedController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserGoalRepository userGoalRepository;

    /**
     * GET /api/feed — returns ranked content recommendations.
     * Uses the most recent goal if no goalId is specified.
     */
    @GetMapping
    public ResponseEntity<?> getFeed(@RequestParam(required = false) String goalId) {
        UserGoal goal;

        if (goalId != null && !goalId.isBlank()) {
            goal = userGoalRepository.findById(goalId).orElse(null);
            if (goal == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Goal not found with id: " + goalId));
            }
        } else {
            // Fall back to the most recent goal
            List<UserGoal> allGoals = userGoalRepository.findAll();
            if (allGoals.isEmpty()) {
                return ResponseEntity.ok(List.of()); // empty feed if no goals
            }
            goal = allGoals.get(allGoals.size() - 1); // latest goal
        }

        List<ScoredContentItem> recommendations = recommendationService.getRecommendations(goal, 10);

        // Transform to the shape the frontend expects
        List<Map<String, Object>> feedItems = recommendations.stream().map(scored -> {
            var item = scored.getItem();
            return Map.<String, Object>ofEntries(
                Map.entry("id", item.getId()),
                Map.entry("title", item.getTitle()),
                Map.entry("source", item.getSource() != null ? item.getSource() : ""),
                Map.entry("format", item.getFormat() != null ? item.getFormat() : ""),
                Map.entry("duration", item.getDurationMinutes() != null
                        ? item.getDurationMinutes() + " min" : ""),
                Map.entry("url", item.getUrl() != null ? item.getUrl() : ""),
                Map.entry("thumbnail", item.getThumbnailUrl() != null ? item.getThumbnailUrl() : ""),
                Map.entry("why_recommended", scored.getWhyRecommended() != null
                        ? scored.getWhyRecommended() : ""),
                Map.entry("matchScore", scored.getMatchScore()),
                Map.entry("description", item.getDescription() != null ? item.getDescription() : ""),
                Map.entry("tags", item.getTags() != null ? item.getTags() : List.of()),
                Map.entry("difficulty", item.getDifficulty() != null ? item.getDifficulty() : "")
            );
        }).toList();

        return ResponseEntity.ok(feedItems);
    }
}
