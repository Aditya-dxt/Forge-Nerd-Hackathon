package com.goalflow.backend.config;

import com.goalflow.backend.model.ContentItem;
import com.goalflow.backend.repository.ContentItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class SeedData {

    @Bean
    CommandLineRunner seedContentItems(ContentItemRepository repo) {
        return args -> {
            if (repo.count() == 0) {

                repo.save(build("youtube", "Spring Boot REST API Crash Course",
                        "https://youtube.com/watch?v=example1",
                        "Fast-paced project-based intro to Spring Boot APIs",
                        List.of("java", "spring-boot", "backend", "rest-api"),
                        "video", 25, "beginner", 85));

                repo.save(build("youtube", "Java Collections Framework Deep Dive",
                        "https://youtube.com/watch?v=example2",
                        "Detailed walkthrough of List, Set, Map internals",
                        List.of("java", "data-structures", "collections"),
                        "video", 40, "intermediate", 78));

                repo.save(build("youtube", "Build a Full Stack App with React and Spring Boot",
                        "https://youtube.com/watch?v=example3",
                        "End-to-end tutorial connecting a React frontend to a Spring Boot backend",
                        List.of("java", "spring-boot", "react", "full-stack"),
                        "video", 90, "intermediate", 92));

                repo.save(build("youtube", "MongoDB Basics for Beginners",
                        "https://youtube.com/watch?v=example4",
                        "Introduction to documents, collections, and queries in MongoDB",
                        List.of("mongodb", "database", "backend"),
                        "video", 20, "beginner", 70));

                repo.save(build("youtube", "System Design Interview: Design a URL Shortener",
                        "https://youtube.com/watch?v=example5",
                        "Walkthrough of designing a scalable URL shortener service",
                        List.of("system-design", "interview-prep", "backend"),
                        "video", 35, "advanced", 88));

                repo.save(build("reddit", "How I structured my first Spring Boot microservices project",
                        "https://reddit.com/r/java/example1",
                        "A discussion thread on folder structure, layering, and common mistakes",
                        List.of("java", "spring-boot", "microservices", "backend"),
                        "discussion", null, "intermediate", 65));

                repo.save(build("reddit", "DSA study plan that actually got me through interviews",
                        "https://reddit.com/r/leetcode/example2",
                        "A redditor shares a structured 8-week DSA prep plan",
                        List.of("dsa", "interview-prep", "algorithms"),
                        "discussion", null, "beginner", 74));

                repo.save(build("reddit", "MongoDB vs PostgreSQL for a hackathon project — thoughts?",
                        "https://reddit.com/r/webdev/example3",
                        "Community discussion comparing tradeoffs for fast prototyping",
                        List.of("mongodb", "postgresql", "database", "web-dev"),
                        "discussion", null, "beginner", 58));

                repo.save(build("reddit", "Tips for writing clean REST API responses",
                        "https://reddit.com/r/java/example4",
                        "Discussion on DTOs, error handling, and consistent response shapes",
                        List.of("java", "rest-api", "backend", "best-practices"),
                        "discussion", null, "intermediate", 61));

                repo.save(build("github", "spring-boot-starter-examples",
                        "https://github.com/example/spring-boot-starter-examples",
                        "A curated repo of minimal Spring Boot starter projects for common use cases",
                        List.of("java", "spring-boot", "backend"),
                        "repo", null, "beginner", 80));

                repo.save(build("github", "awesome-system-design",
                        "https://github.com/example/awesome-system-design",
                        "Curated list of system design resources, case studies, and diagrams",
                        List.of("system-design", "interview-prep"),
                        "repo", null, "advanced", 91));

                repo.save(build("github", "leetcode-java-solutions",
                        "https://github.com/example/leetcode-java-solutions",
                        "Java solutions to common LeetCode problems organized by topic",
                        List.of("dsa", "java", "algorithms", "interview-prep"),
                        "repo", null, "intermediate", 76));

                repo.save(build("github", "react-dashboard-boilerplate",
                        "https://github.com/example/react-dashboard-boilerplate",
                        "A ready-to-use React dashboard template with charts and auth",
                        List.of("react", "frontend", "web-dev"),
                        "repo", null, "beginner", 69));

                repo.save(build("youtube", "Docker for Java Developers",
                        "https://youtube.com/watch?v=example6",
                        "Containerizing a Spring Boot app step by step",
                        List.of("docker", "java", "spring-boot", "devops"),
                        "video", 30, "intermediate", 72));

                repo.save(build("youtube", "Intro to Data Structures and Algorithms in Python",
                        "https://youtube.com/watch?v=example7",
                        "Beginner-friendly overview of core DSA concepts using Python",
                        List.of("dsa", "python", "algorithms"),
                        "video", 45, "beginner", 83));

                repo.save(build("github", "mongodb-nodejs-examples",
                        "https://github.com/example/mongodb-nodejs-examples",
                        "Sample CRUD apps demonstrating MongoDB with Node.js",
                        List.of("mongodb", "nodejs", "database"),
                        "repo", null, "beginner", 63));

                repo.save(build("reddit", "How to approach system design questions as a beginner",
                        "https://reddit.com/r/cscareerquestions/example5",
                        "Thread breaking down a step-by-step approach to system design interviews",
                        List.of("system-design", "interview-prep"),
                        "discussion", null, "beginner", 67));

                repo.save(build("youtube", "Building a Recommendation Engine from Scratch",
                        "https://youtube.com/watch?v=example8",
                        "Explains scoring, tagging, and ranking logic for content recommenders",
                        List.of("algorithms", "backend", "recommendation-systems"),
                        "video", 50, "advanced", 79));
            }
        };
    }

    private ContentItem build(String source, String title, String url, String description,
                               List<String> tags, String format, Integer durationMinutes,
                               String difficulty, int popularityScore) {
        ContentItem item = new ContentItem();
        item.setSource(source);
        item.setTitle(title);
        item.setUrl(url);
        item.setDescription(description);
        item.setTags(tags);
        item.setFormat(format);
        item.setDurationMinutes(durationMinutes);
        item.setDifficulty(difficulty);
        item.setPopularityScore(popularityScore);
        return item;
    }
}