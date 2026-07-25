package com.goalflow.backend.controller;

import com.goalflow.backend.dto.DashboardSummary;
import com.goalflow.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<?> getDashboardSummary(@RequestParam(required = false) String goalId) {
        if (goalId == null || goalId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "goalId query parameter is required"));
        }

        DashboardSummary summary = dashboardService.getSummary(goalId);
        return ResponseEntity.ok(summary);
    }
}
