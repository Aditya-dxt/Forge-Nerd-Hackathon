package com.goalflow.backend.service;

import com.goalflow.backend.dto.UserGoal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Shared helper that builds a clean search query string from a UserGoal.
 * Reusable across all external API calls (GitHub, HN, Reddit, YouTube).
 */
public class QueryBuilder {

    private static final Set<String> STOPWORDS = Set.of(
        "a", "an", "the", "to", "for", "of", "in", "on", "and", "i", "want", "learn",
        "get", "become", "with", "my", "is", "at", "by", "or", "be", "how", "what",
        "this", "that", "it", "do", "can", "about", "study", "master", "understand",
        "basic", "basics", "improve", "start", "begin", "into", "up", "through"
    );

    /**
     * Extract meaningful keywords from a goal title and return a search query string.
     * Example: "Learn Spring Boot and backend development" → "spring boot backend development"
     */
    public static String buildSearchQuery(UserGoal goal) {
        if (goal == null || goal.getGoal() == null) return "";

        String goalText = goal.getGoal();

        List<String> keywords = Arrays.stream(goalText.toLowerCase().split("[^a-zA-Z0-9#+.]+"))
                .filter(w -> !w.isBlank() && w.length() > 1 && !STOPWORDS.contains(w))
                .collect(Collectors.toList());

        if (keywords.isEmpty()) {
            // Fallback: use the whole goal text
            return goalText.trim();
        }

        // Limit to 5 keywords to keep queries focused
        return keywords.stream()
                .limit(5)
                .collect(Collectors.joining(" "));
    }

    /**
     * Extract keyword set (for tag matching in scoring).
     */
    public static Set<String> extractKeywords(String text) {
        if (text == null) return Collections.emptySet();
        return Arrays.stream(text.toLowerCase().split("[^a-zA-Z0-9#+.]+"))
                .filter(w -> !w.isBlank() && !STOPWORDS.contains(w))
                .collect(Collectors.toSet());
    }
}
