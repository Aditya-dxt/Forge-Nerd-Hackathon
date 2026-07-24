package com.goalflow.backend.service;

import com.goalflow.backend.dto.ScoredContentItem;
import com.goalflow.backend.dto.UserGoal;
import com.goalflow.backend.model.ContentItem;
import com.goalflow.backend.repository.ContentItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private ContentItemRepository repository;

    private static final Set<String> STOPWORDS = Set.of(
        "a", "an", "the", "to", "for", "of", "in", "on", "and", "i", "want", "learn",
        "get", "become", "with", "my", "is", "at", "by", "or", "be"
    );

    public List<ScoredContentItem> getRecommendations(UserGoal goal, int limit) {
        List<ContentItem> allItems = repository.findAll();

        Set<String> goalKeywords = extractKeywords(goal.getGoal());
        Set<String> avoidTopics = goal.getAvoidTopics() == null
                ? Collections.emptySet()
                : goal.getAvoidTopics().stream().map(String::toLowerCase).collect(Collectors.toSet());

        List<ScoredContentItem> scored = new ArrayList<>();

        for (ContentItem item : allItems) {
            List<String> itemTags = item.getTags() == null ? Collections.emptyList() : item.getTags();
            Set<String> itemTagsLower = itemTags.stream().map(String::toLowerCase).collect(Collectors.toSet());

            // Skip items touching avoided topics
            boolean touchesAvoided = itemTagsLower.stream().anyMatch(avoidTopics::contains);
            if (touchesAvoided) continue;

            int score = calculateScore(goal, goalKeywords, itemTagsLower, item);
            scored.add(new ScoredContentItem(item, score));
        }

        scored.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        return scored.stream().limit(limit).collect(Collectors.toList());
    }

    private int calculateScore(UserGoal goal, Set<String> goalKeywords, Set<String> itemTagsLower, ContentItem item) {
        int score = 0;

        // 1. Keyword/tag overlap (up to 50 pts)
        long overlapCount = itemTagsLower.stream().filter(goalKeywords::contains).count();
        int overlapScore = (int) Math.min(50, overlapCount * 15);
        score += overlapScore;

        // 2. Difficulty match (20 pts)
        if (goal.getSkillLevel() != null && goal.getSkillLevel().equalsIgnoreCase(item.getDifficulty())) {
            score += 20;
        }

        // 3. Preferred format match (15 pts)
        if (goal.getPreferredFormats() != null && item.getFormat() != null) {
            boolean formatMatch = goal.getPreferredFormats().stream()
                    .anyMatch(f -> f.equalsIgnoreCase(item.getFormat()));
            if (formatMatch) score += 15;
        }

        // 4. Duration fit (10 pts) — only applies if item has a duration (videos)
        if (item.getDurationMinutes() != null && goal.getDailyMinutes() > 0) {
            if (item.getDurationMinutes() <= goal.getDailyMinutes()) {
                score += 10;
            }
        } else if (item.getDurationMinutes() == null) {
            // Non-timed formats (repo/discussion) get partial credit by default
            score += 5;
        }

        // 5. Popularity tiebreaker (up to 5 pts)
        score += Math.round(item.getPopularityScore() / 100f * 5);

        return Math.min(score, 100);
    }

    private Set<String> extractKeywords(String goalText) {
        if (goalText == null) return Collections.emptySet();
        return Arrays.stream(goalText.toLowerCase().split("[^a-zA-Z0-9]+"))
                .filter(w -> !w.isBlank() && !STOPWORDS.contains(w))
                .collect(Collectors.toSet());
    }
}