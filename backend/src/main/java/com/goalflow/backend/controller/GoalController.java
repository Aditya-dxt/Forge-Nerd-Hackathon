package com.goalflow.backend.controller;

import com.goalflow.backend.dto.UserGoal;
import com.goalflow.backend.repository.UserGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class GoalController {

    @Autowired
    private UserGoalRepository userGoalRepository;

    @PostMapping({"/goal", "/api/goals"})
    public ResponseEntity<UserGoal> saveGoal(@RequestBody UserGoal goal) {
        UserGoal saved = userGoalRepository.save(goal);
        return ResponseEntity.ok(saved);
    }

    @GetMapping({"/goal", "/api/goals"})
    public Iterable<UserGoal> getAllGoals() {
        return userGoalRepository.findAll();
    }

    @GetMapping("/api/goals/active")
    public ResponseEntity<?> getActiveGoals() {
        List<UserGoal> all = userGoalRepository.findAll();
        // Return all goals — the frontend uses this to check if any goals exist
        return ResponseEntity.ok(all);
    }
}