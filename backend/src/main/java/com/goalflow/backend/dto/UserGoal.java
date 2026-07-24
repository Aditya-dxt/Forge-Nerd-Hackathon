package com.goalflow.backend.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "userGoals")
public class UserGoal {

    @Id
    private String id;

    private String goal;
    private String deadline;
    private String skillLevel;
    private int dailyMinutes;
    private List<String> avoidTopics;
    private List<String> preferredFormats;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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