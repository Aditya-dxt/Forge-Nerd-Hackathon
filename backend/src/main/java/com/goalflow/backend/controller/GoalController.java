package com.goalflow.backend.controller;

import com.goalflow.backend.dto.UserGoal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goal")
public class GoalController {

    @PostMapping
    public ResponseEntity<String> saveGoal(@RequestBody UserGoal goal) {
        System.out.println("Received goal: " + goal);
        // TODO: persist this — for now just echo confirmation
        return ResponseEntity.ok("Goal saved for: " + goal.getGoal());
    }
}