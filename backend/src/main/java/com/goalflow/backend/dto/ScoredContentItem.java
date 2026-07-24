package com.goalflow.backend.dto;

import com.goalflow.backend.model.ContentItem;

public class ScoredContentItem {
    private ContentItem item;
    private int matchScore;

    public ScoredContentItem(ContentItem item, int matchScore) {
        this.item = item;
        this.matchScore = matchScore;
    }

    public ContentItem getItem() { return item; }
    public void setItem(ContentItem item) { this.item = item; }

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }
}