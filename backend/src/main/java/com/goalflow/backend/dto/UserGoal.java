package com.goalflow.backend.dto;

import java.util.List;

public class UserGoal {
    private String goal;
    private String deadline;
    private String skillLevel;
    private int dailyMinutes;
    private List<String> avoidTopics;
    private List<String> preferredFormats;

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }

    public int getDailyMinutes() { return dailyMinutes; }
    public void setDailyMinutes(int dailyMinutes) { this.dailyMinutes = dailyMinutes; }

    public List<String> getAvoidTopics() { return avoidTopics; }
    public void setAvoidTopics(List<String> avoidTopics) { this.avoidTopics = avoidTopics; }

    public List<String> getPreferredFormats() { return preferredFormats; }
    public void setPreferredFormats(List<String> preferredFormats) { this.preferredFormats = preferredFormats; }
}