package com.goalflow.backend.controller;

import com.goalflow.backend.model.Interaction;
import com.goalflow.backend.repository.InteractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping({"/interactions", "/api/interactions"})
public class InteractionController {

    private static final Set<String> VALID_ACTIONS = Set.of("SHOWN", "CLICKED", "COMPLETED", "SKIPPED");

    @Autowired
    private InteractionRepository interactionRepository;

    @PostMapping
    public ResponseEntity<?> logInteraction(@RequestBody Interaction interaction) {
        // Validate goalId
        if (interaction.getGoalId() == null || interaction.getGoalId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "goalId is required"));
        }

        // Validate action
        if (interaction.getAction() == null || !VALID_ACTIONS.contains(interaction.getAction().toUpperCase())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "action must be one of: SHOWN, CLICKED, COMPLETED, SKIPPED"));
        }

        // Validate that at least contentId or contentTitle is present
        if ((interaction.getContentId() == null || interaction.getContentId().isBlank())
                && (interaction.getContentTitle() == null || interaction.getContentTitle().isBlank())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Either contentId or contentTitle is required"));
        }

        // Normalize action to uppercase
        interaction.setAction(interaction.getAction().toUpperCase());

        // Set timestamp if not already set
        if (interaction.getTimestamp() == null) {
            interaction.setTimestamp(Instant.now());
        }

        Interaction saved = interactionRepository.save(interaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
