package com.goalflow.backend.repository;

   import com.goalflow.backend.model.ContentItem;
   import org.springframework.data.mongodb.repository.MongoRepository;

   public interface ContentItemRepository extends MongoRepository<ContentItem, String> {
   }