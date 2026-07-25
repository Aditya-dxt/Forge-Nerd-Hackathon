package com.goalflow.backend.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reflections")
public class Reflection {

    @Id
    private String id;

    private String goalId;
    private boolean understood;
    private boolean gotDistracted;
    private String difficultTopic;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }

    public boolean isUnderstood() { return understood; }
    public void setUnderstood(boolean understood) { this.understood = understood; }

    public boolean isGotDistracted() { return gotDistracted; }
    public void setGotDistracted(boolean gotDistracted) { this.gotDistracted = gotDistracted; }

    public String getDifficultTopic() { return difficultTopic; }
    public void setDifficultTopic(String difficultTopic) { this.difficultTopic = difficultTopic; }
}