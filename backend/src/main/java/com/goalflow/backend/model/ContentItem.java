package com.goalflow.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "contentItems")
public class ContentItem {
    @Id
    private String id;
    private String source;       // youtube/reddit/github
    private String title;
    private String url;
    private String description;
    private String thumbnailUrl;
    private List<String> tags;
    private String format;       // video/article/repo/discussion
    private Integer durationMinutes;
    private String difficulty;   // beginner/intermediate/advanced
    private int popularityScore;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public int getPopularityScore() { return popularityScore; }
    public void setPopularityScore(int popularityScore) { this.popularityScore = popularityScore; }
}