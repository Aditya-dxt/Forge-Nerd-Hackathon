package com.goalflow.backend.controller;

import com.goalflow.backend.dto.ScoredContentItem;
import com.goalflow.backend.dto.UserGoal;
import com.goalflow.backend.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @PostMapping
    public List<ScoredContentItem> getRecommendations(@RequestBody UserGoal goal) {
        return recommendationService.getRecommendations(goal, 10);
    }
}