package com.goalflow.backend.repository;

import com.goalflow.backend.dto.UserGoal;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserGoalRepository extends MongoRepository<UserGoal, String> {
}