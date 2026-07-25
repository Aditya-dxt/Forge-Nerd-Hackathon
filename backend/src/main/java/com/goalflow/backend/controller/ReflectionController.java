package com.goalflow.backend.controller;

import com.goalflow.backend.model.Reflection;
import com.goalflow.backend.repository.ReflectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping({"/reflections", "/api/reflections"})
public class ReflectionController {

    @Autowired
    private ReflectionRepository reflectionRepository;

    @PostMapping
    public ResponseEntity<?> submitReflection(@RequestBody Reflection reflection) {
        // Validate goalId
        if (reflection.getGoalId() == null || reflection.getGoalId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "goalId is required"));
        }

        // Set timestamp if not already set
        if (reflection.getTimestamp() == null) {
            reflection.setTimestamp(Instant.now());
        }

        Reflection saved = reflectionRepository.save(reflection);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
