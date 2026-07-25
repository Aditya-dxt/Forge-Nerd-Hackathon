package com.goalflow.backend.dto;

import com.goalflow.backend.model.ContentItem;

public class ScoredContentItem {
    private ContentItem item;
    private int matchScore;
    private String whyRecommended;

    public ScoredContentItem(ContentItem item, int matchScore, String whyRecommended) {
        this.item = item;
        this.matchScore = matchScore;
        this.whyRecommended = whyRecommended;
    }

    public ContentItem getItem() { return item; }
    public void setItem(ContentItem item) { this.item = item; }

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }

    public String getWhyRecommended() { return whyRecommended; }
    public void setWhyRecommended(String whyRecommended) { this.whyRecommended = whyRecommended; }
}