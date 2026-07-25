package com.goalflow.backend.controller;

import com.goalflow.backend.dto.Reflection;
import com.goalflow.backend.repository.ReflectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/reflection")
public class ReflectionController {

    @Autowired
    private ReflectionRepository reflectionRepository;

    @PostMapping
    public Reflection saveReflection(@RequestBody Reflection reflection) {
        return reflectionRepository.save(reflection);
    }

    @GetMapping("/{goalId}")
    public List<Reflection> getReflectionsForGoal(@PathVariable String goalId) {
        return reflectionRepository.findByGoalId(goalId);
    }
}