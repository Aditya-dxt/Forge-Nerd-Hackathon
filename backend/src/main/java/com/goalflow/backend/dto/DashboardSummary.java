package com.goalflow.backend.dto;

public class DashboardSummary {

    private int totalItemsCompleted;
    private int totalItemsSkipped;
    private int currentStreak;
    private int totalTimeSpentEstimateMinutes;
    private double understoodRate;

    // Getters and setters
    public int getTotalItemsCompleted() { return totalItemsCompleted; }
    public void setTotalItemsCompleted(int totalItemsCompleted) { this.totalItemsCompleted = totalItemsCompleted; }

    public int getTotalItemsSkipped() { return totalItemsSkipped; }
    public void setTotalItemsSkipped(int totalItemsSkipped) { this.totalItemsSkipped = totalItemsSkipped; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getTotalTimeSpentEstimateMinutes() { return totalTimeSpentEstimateMinutes; }
    public void setTotalTimeSpentEstimateMinutes(int totalTimeSpentEstimateMinutes) {
        this.totalTimeSpentEstimateMinutes = totalTimeSpentEstimateMinutes;
    }

    public double getUnderstoodRate() { return understoodRate; }
    public void setUnderstoodRate(double understoodRate) { this.understoodRate = understoodRate; }
}
