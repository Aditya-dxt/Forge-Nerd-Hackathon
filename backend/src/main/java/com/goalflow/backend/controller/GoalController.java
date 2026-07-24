package com.goalflow.backend.controller;

import com.goalflow.backend.dto.UserGoal;
import com.goalflow.backend.repository.UserGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/goal")
public class GoalController {

    @Autowired
    private UserGoalRepository userGoalRepository;

    @PostMapping
    public ResponseEntity<UserGoal> saveGoal(@RequestBody UserGoal goal) {
        UserGoal saved = userGoalRepository.save(goal);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public Iterable<UserGoal> getAllGoals() {
        return userGoalRepository.findAll();
    }
}