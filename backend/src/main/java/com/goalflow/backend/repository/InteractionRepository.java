package com.goalflow.backend.repository;

import com.goalflow.backend.model.Interaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface InteractionRepository extends MongoRepository<Interaction, String> {
    List<Interaction> findByGoalId(String goalId);
    List<Interaction> findByGoalIdAndAction(String goalId, String action);
}
