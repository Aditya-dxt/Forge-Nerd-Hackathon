package com.goalflow.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "interactions")
public class Interaction {

    @Id
    private String id;
    private String goalId;
    private String contentId;
    private String contentTitle;
    private String source;
    private String action; // SHOWN, CLICKED, COMPLETED, SKIPPED
    private Instant timestamp;

    public Interaction() {
        this.timestamp = Instant.now();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getContentTitle() { return contentTitle; }
    public void setContentTitle(String contentTitle) { this.contentTitle = contentTitle; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
