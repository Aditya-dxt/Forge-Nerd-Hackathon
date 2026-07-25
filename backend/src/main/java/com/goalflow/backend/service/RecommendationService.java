package com.goalflow.backend.service;

import com.goalflow.backend.dto.ScoredContentItem;
import com.goalflow.backend.dto.UserGoal;
import com.goalflow.backend.model.ContentItem;
import com.goalflow.backend.model.Interaction;
import com.goalflow.backend.model.Reflection;
import com.goalflow.backend.repository.ContentItemRepository;
import com.goalflow.backend.repository.InteractionRepository;
import com.goalflow.backend.repository.ReflectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private ContentItemRepository repository;

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private ReflectionRepository reflectionRepository;

    private static final Set<String> STOPWORDS = Set.of(
        "a", "an", "the", "to", "for", "of", "in", "on", "and", "i", "want", "learn",
        "get", "become", "with", "my", "is", "at", "by", "or", "be"
    );

    /**
     * Get adaptive recommendations for a goal. Factors in past interactions
     * and reflections to personalize scoring and explanations.
     */
    public List<ScoredContentItem> getRecommendations(UserGoal goal, int limit) {
        List<ContentItem> allItems = repository.findAll();

        Set<String> goalKeywords = extractKeywords(goal.getGoal());
        Set<String> avoidTopics = goal.getAvoidTopics() == null
                ? Collections.emptySet()
                : goal.getAvoidTopics().stream().map(String::toLowerCase).collect(Collectors.toSet());

        // Pull behavioral data for adaptive scoring
        String goalId = goal.getId();
        BehavioralProfile profile = buildBehavioralProfile(goalId);

        List<ScoredContentItem> scored = new ArrayList<>();

        for (ContentItem item : allItems) {
            List<String> itemTags = item.getTags() == null ? Collections.emptyList() : item.getTags();
            Set<String> itemTagsLower = itemTags.stream().map(String::toLowerCase).collect(Collectors.toSet());

            // Skip items touching avoided topics
            boolean touchesAvoided = itemTagsLower.stream().anyMatch(avoidTopics::contains);
            if (touchesAvoided) continue;

            // Skip items the user has already completed or skipped
            if (profile.completedContentIds.contains(item.getId())
                    || profile.skippedContentIds.contains(item.getId())) {
                continue;
            }

            ScoringResult result = calculateAdaptiveScore(goal, goalKeywords, itemTagsLower, item, profile);
            scored.add(new ScoredContentItem(item, result.score, result.whyRecommended));
        }

        scored.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        return scored.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Build a behavioral profile from past interactions and reflections.
     */
    private BehavioralProfile buildBehavioralProfile(String goalId) {
        BehavioralProfile profile = new BehavioralProfile();
        if (goalId == null) return profile;

        List<Interaction> interactions = interactionRepository.findByGoalId(goalId);
        List<Reflection> reflections = reflectionRepository.findByGoalIdOrderByTimestampDesc(goalId);

        for (Interaction interaction : interactions) {
            String contentId = interaction.getContentId();
            if (contentId == null) continue;

            switch (interaction.getAction()) {
                case "COMPLETED":
                    profile.completedContentIds.add(contentId);
                    break;
                case "SKIPPED":
                    profile.skippedContentIds.add(contentId);
                    break;
                case "CLICKED":
                    profile.clickedContentIds.add(contentId);
                    break;
            }
        }

        // Resolve completed content items to learn format/difficulty/tag preferences
        if (!profile.completedContentIds.isEmpty()) {
            List<ContentItem> completedItems = repository.findAllById(profile.completedContentIds);
            for (ContentItem ci : completedItems) {
                if (ci.getFormat() != null) {
                    profile.completedFormats.merge(ci.getFormat().toLowerCase(), 1, Integer::sum);
                }
                if (ci.getDifficulty() != null) {
                    profile.completedDifficulties.merge(ci.getDifficulty().toLowerCase(), 1, Integer::sum);
                }
                if (ci.getTags() != null) {
                    for (String tag : ci.getTags()) {
                        profile.completedTags.merge(tag.toLowerCase(), 1, Integer::sum);
                    }
                }
            }
        }

        // Resolve skipped items to learn anti-preferences
        if (!profile.skippedContentIds.isEmpty()) {
            List<ContentItem> skippedItems = repository.findAllById(profile.skippedContentIds);
            for (ContentItem si : skippedItems) {
                if (si.getFormat() != null) {
                    profile.skippedFormats.merge(si.getFormat().toLowerCase(), 1, Integer::sum);
                }
                if (si.getDifficulty() != null) {
                    profile.skippedDifficulties.merge(si.getDifficulty().toLowerCase(), 1, Integer::sum);
                }
            }
        }

        // Check most recent reflection
        if (!reflections.isEmpty()) {
            profile.lastReflectionUnderstood = reflections.get(0).isUnderstood();
            profile.hasReflections = true;
        }

        return profile;
    }

    /**
     * Score a content item with adaptive logic based on behavioral profile.
     */
    private ScoringResult calculateAdaptiveScore(UserGoal goal, Set<String> goalKeywords,
                                                  Set<String> itemTagsLower, ContentItem item,
                                                  BehavioralProfile profile) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // 1. Keyword/tag overlap (up to 50 pts)
        long overlapCount = itemTagsLower.stream().filter(goalKeywords::contains).count();
        int overlapScore = (int) Math.min(50, overlapCount * 15);
        score += overlapScore;
        if (overlapCount > 0) {
            Set<String> matchedTags = itemTagsLower.stream().filter(goalKeywords::contains).collect(Collectors.toSet());
            reasons.add("Matches your goal keywords: " + String.join(", ", matchedTags));
        }

        // 2. Difficulty match (20 pts)
        String effectiveDifficulty = goal.getSkillLevel();

        // Adaptive: if last reflection was understood=false, bias toward easier difficulty
        if (profile.hasReflections && !profile.lastReflectionUnderstood) {
            effectiveDifficulty = lowerDifficulty(effectiveDifficulty);
        }

        if (effectiveDifficulty != null && effectiveDifficulty.equalsIgnoreCase(item.getDifficulty())) {
            score += 20;
            if (profile.hasReflections && !profile.lastReflectionUnderstood
                    && !effectiveDifficulty.equalsIgnoreCase(goal.getSkillLevel())) {
                reasons.add("Adjusted to easier level based on your recent reflection");
            } else {
                reasons.add("Matches your " + effectiveDifficulty + " skill level");
            }
        }

        // 3. Preferred format match (15 pts)
        if (goal.getPreferredFormats() != null && item.getFormat() != null) {
            boolean formatMatch = goal.getPreferredFormats().stream()
                    .anyMatch(f -> f.equalsIgnoreCase(item.getFormat()));
            if (formatMatch) {
                score += 15;
                reasons.add("In your preferred format: " + item.getFormat());
            }
        }

        // 4. Duration fit (10 pts)
        if (item.getDurationMinutes() != null && goal.getDailyMinutes() > 0) {
            if (item.getDurationMinutes() <= goal.getDailyMinutes()) {
                score += 10;
                reasons.add("Fits within your daily " + goal.getDailyMinutes() + "-minute window");
            }
        } else if (item.getDurationMinutes() == null) {
            score += 5;
        }

        // 5. Popularity tiebreaker (up to 5 pts)
        score += Math.round(item.getPopularityScore() / 100f * 5);

        // === ADAPTIVE SCORING BONUSES ===

        // 6. Behavioral format boost: prefer formats the user has completed before (up to 10 pts)
        if (item.getFormat() != null && profile.completedFormats.containsKey(item.getFormat().toLowerCase())) {
            int completedCount = profile.completedFormats.get(item.getFormat().toLowerCase());
            int formatBoost = Math.min(10, completedCount * 3);
            score += formatBoost;
            reasons.add("You've successfully completed " + completedCount + " " + item.getFormat() + " items before");
        }

        // 7. Behavioral tag boost: prefer tags the user has engaged with (up to 10 pts)
        if (item.getTags() != null) {
            long tagOverlap = item.getTags().stream()
                    .map(String::toLowerCase)
                    .filter(profile.completedTags::containsKey)
                    .count();
            if (tagOverlap > 0) {
                int tagBoost = (int) Math.min(10, tagOverlap * 3);
                score += tagBoost;
                reasons.add("Builds on topics you've previously completed");
            }
        }

        // 8. Anti-preference penalty: demote formats/difficulties the user tends to skip
        if (item.getFormat() != null && profile.skippedFormats.containsKey(item.getFormat().toLowerCase())) {
            int skippedCount = profile.skippedFormats.get(item.getFormat().toLowerCase());
            int penalty = Math.min(10, skippedCount * 3);
            score -= penalty;
        }
        if (item.getDifficulty() != null && profile.skippedDifficulties.containsKey(item.getDifficulty().toLowerCase())) {
            int skippedCount = profile.skippedDifficulties.get(item.getDifficulty().toLowerCase());
            int penalty = Math.min(8, skippedCount * 2);
            score -= penalty;
        }

        // 9. Reflection-based difficulty bias: if user didn't understand, boost beginner content
        if (profile.hasReflections && !profile.lastReflectionUnderstood) {
            if ("beginner".equalsIgnoreCase(item.getDifficulty())) {
                score += 15;
                reasons.add("Simpler content recommended after your recent reflection");
            } else if ("advanced".equalsIgnoreCase(item.getDifficulty())) {
                score -= 10; // penalize advanced content when struggling
            }

            // Also prefer shorter content when struggling
            if (item.getDurationMinutes() != null && item.getDurationMinutes() <= 25) {
                score += 5;
                reasons.add("Shorter format to help build confidence");
            }
        }

        score = Math.max(0, Math.min(score, 100));

        // Build the "why recommended" string
        String whyRecommended;
        if (reasons.isEmpty()) {
            whyRecommended = "Popular content relevant to your learning goals";
        } else {
            whyRecommended = String.join(". ", reasons);
        }

        return new ScoringResult(score, whyRecommended);
    }

    /**
     * Lower difficulty by one step: advanced -> intermediate, intermediate -> beginner.
     */
    private String lowerDifficulty(String difficulty) {
        if (difficulty == null) return "beginner";
        switch (difficulty.toLowerCase()) {
            case "advanced": return "intermediate";
            case "intermediate": return "beginner";
            default: return "beginner";
        }
    }

    private Set<String> extractKeywords(String goalText) {
        if (goalText == null) return Collections.emptySet();
        return Arrays.stream(goalText.toLowerCase().split("[^a-zA-Z0-9]+"))
                .filter(w -> !w.isBlank() && !STOPWORDS.contains(w))
                .collect(Collectors.toSet());
    }

    // --- Inner classes ---

    private static class ScoringResult {
        final int score;
        final String whyRecommended;

        ScoringResult(int score, String whyRecommended) {
            this.score = score;
            this.whyRecommended = whyRecommended;
        }
    }

    private static class BehavioralProfile {
        Set<String> completedContentIds = new HashSet<>();
        Set<String> skippedContentIds = new HashSet<>();
        Set<String> clickedContentIds = new HashSet<>();

        Map<String, Integer> completedFormats = new HashMap<>();
        Map<String, Integer> completedDifficulties = new HashMap<>();
        Map<String, Integer> completedTags = new HashMap<>();

        Map<String, Integer> skippedFormats = new HashMap<>();
        Map<String, Integer> skippedDifficulties = new HashMap<>();

        boolean hasReflections = false;
        boolean lastReflectionUnderstood = true;
    }
}