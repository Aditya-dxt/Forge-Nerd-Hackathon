package com.goalflow.backend.service;

import com.goalflow.backend.dto.DashboardSummary;
import com.goalflow.backend.model.ContentItem;
import com.goalflow.backend.model.Interaction;
import com.goalflow.backend.model.Reflection;
import com.goalflow.backend.repository.ContentItemRepository;
import com.goalflow.backend.repository.InteractionRepository;
import com.goalflow.backend.repository.ReflectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private ReflectionRepository reflectionRepository;

    @Autowired
    private ContentItemRepository contentItemRepository;

    /**
     * Compute dashboard summary stats for a given goal.
     */
    public DashboardSummary getSummary(String goalId) {
        List<Interaction> allInteractions = interactionRepository.findByGoalId(goalId);
        List<Reflection> allReflections = reflectionRepository.findByGoalId(goalId);

        DashboardSummary summary = new DashboardSummary();

        // Count completed and skipped
        List<Interaction> completed = allInteractions.stream()
                .filter(i -> "COMPLETED".equals(i.getAction()))
                .collect(Collectors.toList());
        long skipped = allInteractions.stream()
                .filter(i -> "SKIPPED".equals(i.getAction()))
                .count();

        summary.setTotalItemsCompleted(completed.size());
        summary.setTotalItemsSkipped((int) skipped);

        // Current streak: consecutive days with at least one COMPLETED interaction
        summary.setCurrentStreak(calculateStreak(completed));

        // Estimate total time spent from completed items
        summary.setTotalTimeSpentEstimateMinutes(estimateTimeSpent(completed));

        // Understood rate: % of reflections where understood=true
        if (!allReflections.isEmpty()) {
            long understoodCount = allReflections.stream()
                    .filter(Reflection::isUnderstood)
                    .count();
            double rate = (double) understoodCount / allReflections.size() * 100.0;
            summary.setUnderstoodRate(Math.round(rate * 10.0) / 10.0); // one decimal place
        } else {
            summary.setUnderstoodRate(0.0);
        }

        return summary;
    }

    /**
     * Calculate current streak as consecutive days (ending today or yesterday)
     * with at least one COMPLETED interaction.
     */
    private int calculateStreak(List<Interaction> completed) {
        if (completed.isEmpty()) return 0;

        // Collect unique dates of completed interactions
        Set<LocalDate> completedDates = completed.stream()
                .map(i -> i.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        int streak = 0;

        // Start from today and count consecutive days backward
        LocalDate checkDate = today;

        // If today has no completions, check if yesterday does (allow a 1-day grace)
        if (!completedDates.contains(today)) {
            checkDate = today.minusDays(1);
            if (!completedDates.contains(checkDate)) {
                return 0; // no recent activity
            }
        }

        while (completedDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    /**
     * Estimate total time spent by looking up content items for completed interactions.
     * Uses the item's durationMinutes if available, otherwise estimates 10 minutes
     * for repos/discussions.
     */
    private int estimateTimeSpent(List<Interaction> completed) {
        if (completed.isEmpty()) return 0;

        // Collect all contentIds from completed interactions
        Set<String> completedContentIds = completed.stream()
                .map(Interaction::getContentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Look up content items to get durations
        Map<String, Integer> idToDuration = new HashMap<>();
        if (!completedContentIds.isEmpty()) {
            List<ContentItem> items = contentItemRepository.findAllById(completedContentIds);
            for (ContentItem item : items) {
                idToDuration.put(item.getId(), item.getDurationMinutes());
            }
        }

        int totalMinutes = 0;
        for (Interaction interaction : completed) {
            String cid = interaction.getContentId();
            Integer duration = cid != null ? idToDuration.get(cid) : null;
            if (duration != null) {
                totalMinutes += duration;
            } else {
                // Estimate 10 minutes for items without a known duration
                totalMinutes += 10;
            }
        }

        return totalMinutes;
    }
}
