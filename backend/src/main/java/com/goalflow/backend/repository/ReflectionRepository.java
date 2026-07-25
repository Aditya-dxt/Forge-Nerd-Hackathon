package com.goalflow.backend.repository;

import com.goalflow.backend.dto.Reflection;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReflectionRepository extends MongoRepository<Reflection, String> {
    List<Reflection> findByGoalId(String goalId);
}