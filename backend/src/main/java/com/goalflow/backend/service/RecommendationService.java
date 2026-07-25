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

    @Autowired
    private LiveContentService liveContentService;

    private static final Set<String> STOPWORDS = Set.of(
        "a", "an", "the", "to", "for", "of", "in", "on", "and", "i", "want", "learn",
        "get", "become", "with", "my", "is", "at", "by", "or", "be"
    );

    // Max number of entertainment items guaranteed a slot in the final feed
    private static final int MAX_ENTERTAINMENT_SLOTS = 2;

    /**
     * Get adaptive recommendations for a goal. Fetches live content from external
     * APIs (GitHub, HN, Reddit, YouTube) and scores them using behavioral profile data.
     * Guarantees a small number of entertainment items are mixed in, per the
     * "balanced content strategy" design goal.
     */
    public List<ScoredContentItem> getRecommendations(UserGoal goal, int limit) {
        List<ContentItem> allItems = liveContentService.getLiveContent(goal);

        Set<String> goalKeywords = extractKeywords(goal.getGoal());
        Set<String> avoidTopics = goal.getAvoidTopics() == null
                ? Collections.emptySet()
                : goal.getAvoidTopics().stream().map(String::toLowerCase).collect(Collectors.toSet());

        String goalId = goal.getId();
        BehavioralProfile profile = buildBehavioralProfile(goalId);

        List<ScoredContentItem> learningScored = new ArrayList<>();
        List<ScoredContentItem> entertainmentScored = new ArrayList<>();

        for (ContentItem item : allItems) {
            List<String> itemTags = item.getTags() == null ? Collections.emptyList() : item.getTags();
            Set<String> itemTagsLower = itemTags.stream().map(String::toLowerCase).collect(Collectors.toSet());

            boolean touchesAvoided = itemTagsLower.stream().anyMatch(avoidTopics::contains);
            if (touchesAvoided) continue;

            if (profile.completedContentIds.contains(item.getId())
                    || profile.skippedContentIds.contains(item.getId())) {
                continue;
            }

            ScoringResult result = calculateAdaptiveScore(goal, goalKeywords, itemTagsLower, item, profile);
            ScoredContentItem scoredItem = new ScoredContentItem(item, result.score, result.whyRecommended);

            if ("entertainment".equalsIgnoreCase(item.getFormat())) {
                entertainmentScored.add(scoredItem);
            } else {
                learningScored.add(scoredItem);
            }
        }

        learningScored.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));
        entertainmentScored.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        // Reserve a small number of slots for entertainment so it doesn't get
        // crowded out by higher-scoring learning content (balanced content strategy)
        int entertainmentSlots = Math.min(MAX_ENTERTAINMENT_SLOTS, entertainmentScored.size());
        int learningSlots = Math.max(0, limit - entertainmentSlots);

        List<ScoredContentItem> finalList = new ArrayList<>();
        finalList.addAll(learningScored.stream().limit(learningSlots).collect(Collectors.toList()));
        finalList.addAll(entertainmentScored.stream().limit(entertainmentSlots).collect(Collectors.toList()));

        // Re-sort the combined list by score so entertainment items appear
        // interleaved naturally rather than always at the end
        finalList.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

        return finalList.stream().limit(limit).collect(Collectors.toList());
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

        long overlapCount = itemTagsLower.stream().filter(goalKeywords::contains).count();
        int overlapScore = (int) Math.min(50, overlapCount * 15);
        score += overlapScore;
        if (overlapCount > 0) {
            Set<String> matchedTags = itemTagsLower.stream().filter(goalKeywords::contains).collect(Collectors.toSet());
            reasons.add("Matches your goal keywords: " + String.join(", ", matchedTags));
        }

        String effectiveDifficulty = goal.getSkillLevel();

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

        if (goal.getPreferredFormats() != null && item.getFormat() != null) {
            boolean formatMatch = goal.getPreferredFormats().stream()
                    .anyMatch(f -> f.equalsIgnoreCase(item.getFormat()));
            if (formatMatch) {
                score += 15;
                reasons.add("In your preferred format: " + item.getFormat());
            }
        }

        if (item.getDurationMinutes() != null && goal.getDailyMinutes() > 0) {
            if (item.getDurationMinutes() <= goal.getDailyMinutes()) {
                score += 10;
                reasons.add("Fits within your daily " + goal.getDailyMinutes() + "-minute window");
            }
        } else if (item.getDurationMinutes() == null) {
            score += 5;
        }

        score += Math.round(item.getPopularityScore() / 100f * 5);

        if (item.getFormat() != null && profile.completedFormats.containsKey(item.getFormat().toLowerCase())) {
            int completedCount = profile.completedFormats.get(item.getFormat().toLowerCase());
            int formatBoost = Math.min(10, completedCount * 3);
            score += formatBoost;
            reasons.add("You've successfully completed " + completedCount + " " + item.getFormat() + " items before");
        }

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

        if (profile.hasReflections && !profile.lastReflectionUnderstood) {
            if ("beginner".equalsIgnoreCase(item.getDifficulty())) {
                score += 15;
                reasons.add("Simpler content recommended after your recent reflection");
            } else if ("advanced".equalsIgnoreCase(item.getDifficulty())) {
                score -= 10;
            }

            if (item.getDurationMinutes() != null && item.getDurationMinutes() <= 25) {
                score += 5;
                reasons.add("Shorter format to help build confidence");
            }
        }

        // Entertainment items get a distinct "why recommended" reason if nothing else matched
        if ("entertainment".equalsIgnoreCase(item.getFormat()) && reasons.isEmpty()) {
            reasons.add("A lighter break, still connected to your goal");
        }

        score = Math.max(0, Math.min(score, 100));

        String whyRecommended;
        if (reasons.isEmpty()) {
            whyRecommended = "Popular content relevant to your learning goals";
        } else {
            whyRecommended = String.join(". ", reasons);
        }

        return new ScoringResult(score, whyRecommended);
    }

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