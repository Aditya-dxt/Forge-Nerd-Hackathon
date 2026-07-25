package com.goalflow.backend.service;

import com.goalflow.backend.dto.UserGoal;
import com.goalflow.backend.model.ContentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fetches live content from external APIs (GitHub, HN, Reddit, YouTube) and maps
 * results to ContentItem objects for the scoring pipeline. Includes an
 * in-memory cache with 10-minute TTL to avoid rate limits during demos.
 */
@Service
public class LiveContentService {

    private static final Logger log = LoggerFactory.getLogger(LiveContentService.class);

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final WebClient webClient;

    // In-memory cache: query -> (timestamp, results)
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Value("${YOUTUBE_API_KEY:#{null}}")
    private String youtubeApiKey;

    public LiveContentService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();
    }

    /**
     * Get all live content for a goal, aggregated from all sources.
     * Results are cached per query string for CACHE_TTL.
     */
    public List<ContentItem> getLiveContent(UserGoal goal) {
        String query = QueryBuilder.buildSearchQuery(goal);
        if (query.isBlank()) {
            log.warn("Empty query built from goal, returning empty content");
            return Collections.emptyList();
        }

        // Check cache
        CacheEntry cached = cache.get(query);
        if (cached != null && !cached.isExpired()) {
            log.info("Cache hit for query '{}' ({} items)", query, cached.items.size());
            return cached.items;
        }

        log.info("Fetching live content for query: '{}'", query);
        List<ContentItem> allItems = new ArrayList<>();

        // Fetch from each source independently — failure in one doesn't break others
        allItems.addAll(fetchGitHub(query, goal));
        allItems.addAll(fetchHackerNews(query, goal));
        allItems.addAll(fetchReddit(query, goal));
        allItems.addAll(fetchYouTube(query, goal));
        allItems.addAll(fetchEntertainment(query, goal));

        // Cache the aggregated results
        cache.put(query, new CacheEntry(allItems));
        log.info("Cached {} total items for query '{}'", allItems.size(), query);

        return allItems;
    }

    // ======================== GITHUB ========================

    @SuppressWarnings("unchecked")
    private List<ContentItem> fetchGitHub(String query, UserGoal goal) {
        try {
            log.info("Fetching GitHub repos for query: '{}'", query);
            String encodedQuery = java.net.URLEncoder.encode(query + " tutorial", java.nio.charset.StandardCharsets.UTF_8);

            Map<String, Object> response = webClient.get()
                    .uri("https://api.github.com/search/repositories?q=" + encodedQuery + "&sort=stars&per_page=10")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Forge-Hackathon-App/1.0")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || !response.containsKey("items")) {
                log.warn("GitHub returned no items");
                return Collections.emptyList();
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            List<ContentItem> results = new ArrayList<>();

            for (Map<String, Object> repo : items) {
                ContentItem item = new ContentItem();
                item.setId("gh-" + repo.get("id"));
                item.setTitle((String) repo.get("full_name"));
                item.setUrl((String) repo.get("html_url"));
                item.setDescription((String) repo.get("description"));
                item.setSource("github");
                item.setFormat("repo");
                item.setDurationMinutes(null); // repos don't have duration
                item.setDifficulty(goal.getSkillLevel() != null ? goal.getSkillLevel() : "intermediate");

                // Build tags from language + topics
                List<String> tags = new ArrayList<>();
                String language = (String) repo.get("language");
                if (language != null) tags.add(language.toLowerCase());
                Object topicsObj = repo.get("topics");
                if (topicsObj instanceof List) {
                    for (Object t : (List<?>) topicsObj) {
                        tags.add(String.valueOf(t).toLowerCase());
                    }
                }
                item.setTags(tags);

                // Normalize stars to 0-100 popularity score
                int stars = repo.get("stargazers_count") instanceof Number
                        ? ((Number) repo.get("stargazers_count")).intValue() : 0;
                item.setPopularityScore(normalizePopularity(stars, 50000));

                results.add(item);
            }

            log.info("GitHub returned {} items", results.size());
            return results;

        } catch (Exception e) {
            log.error("GitHub API failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ======================== HACKER NEWS ========================

    @SuppressWarnings("unchecked")
    private List<ContentItem> fetchHackerNews(String query, UserGoal goal) {
        try {
            log.info("Fetching Hacker News stories for query: '{}'", query);
            String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);

            Map<String, Object> response = webClient.get()
                    .uri("https://hn.algolia.com/api/v1/search?query=" + encodedQuery + "&tags=story&hitsPerPage=10")
                    .header("User-Agent", "Forge-Hackathon-App/1.0")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || !response.containsKey("hits")) {
                log.warn("Hacker News returned no hits");
                return Collections.emptyList();
            }

            List<Map<String, Object>> hits = (List<Map<String, Object>>) response.get("hits");
            List<ContentItem> results = new ArrayList<>();

            for (Map<String, Object> hit : hits) {
                String storyUrl = (String) hit.get("url");
                String objectID = (String) hit.get("objectID");

                if (storyUrl == null || storyUrl.isBlank()) {
                    storyUrl = "https://news.ycombinator.com/item?id=" + objectID;
                }

                ContentItem item = new ContentItem();
                item.setId("hn-" + objectID);
                item.setTitle((String) hit.get("title"));
                item.setUrl(storyUrl);
                item.setDescription((String) hit.get("title"));
                item.setSource("hackernews");
                item.setFormat("discussion");
                item.setDurationMinutes(null);
                item.setDifficulty(goal.getSkillLevel() != null ? goal.getSkillLevel() : "intermediate");

                item.setTags(new ArrayList<>(QueryBuilder.extractKeywords(
                        (String) hit.get("title"))));

                int points = hit.get("points") instanceof Number
                        ? ((Number) hit.get("points")).intValue() : 0;
                item.setPopularityScore(normalizePopularity(points, 5000));

                results.add(item);
            }

            log.info("Hacker News returned {} items", results.size());
            return results;

        } catch (Exception e) {
            log.error("Hacker News API failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ======================== REDDIT ========================

    @SuppressWarnings("unchecked")
    private List<ContentItem> fetchReddit(String query, UserGoal goal) {
        try {
            log.info("Fetching Reddit posts for query: '{}'", query);
            String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);

            Map<String, Object> response = webClient.get()
                    .uri("https://www.reddit.com/search.json?q=" + encodedQuery + "&sort=relevance&limit=10")
                    .header("User-Agent", "Forge-Hackathon-App/1.0")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || !response.containsKey("data")) {
                log.warn("Reddit returned no data");
                return Collections.emptyList();
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            List<Map<String, Object>> children = (List<Map<String, Object>>) data.get("children");

            if (children == null) return Collections.emptyList();

            List<ContentItem> results = new ArrayList<>();

            for (Map<String, Object> child : children) {
                Map<String, Object> post = (Map<String, Object>) child.get("data");
                if (post == null) continue;

                String postId = (String) post.get("id");
                String permalink = (String) post.get("permalink");
                String postUrl = permalink != null ? "https://www.reddit.com" + permalink : "";

                ContentItem item = new ContentItem();
                item.setId("reddit-" + postId);
                item.setTitle((String) post.get("title"));
                item.setUrl(postUrl);

                String selftext = (String) post.get("selftext");
                item.setDescription(selftext != null && selftext.length() > 200
                        ? selftext.substring(0, 200) + "..."
                        : selftext);

                item.setSource("reddit");
                item.setFormat("discussion");
                item.setDurationMinutes(null);
                item.setDifficulty(goal.getSkillLevel() != null ? goal.getSkillLevel() : "intermediate");

                List<String> tags = new ArrayList<>();
                String subreddit = (String) post.get("subreddit");
                if (subreddit != null) tags.add(subreddit.toLowerCase());
                tags.addAll(QueryBuilder.extractKeywords((String) post.get("title")));
                item.setTags(tags);

                int score = post.get("score") instanceof Number
                        ? ((Number) post.get("score")).intValue() : 0;
                item.setPopularityScore(normalizePopularity(score, 10000));

                String thumbnail = (String) post.get("thumbnail");
                if (thumbnail != null && thumbnail.startsWith("http")) {
                    item.setThumbnailUrl(thumbnail);
                }

                results.add(item);
            }

            log.info("Reddit returned {} items", results.size());
            return results;

        } catch (Exception e) {
            log.error("Reddit API failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ======================== YOUTUBE ========================

    @SuppressWarnings("unchecked")
    private List<ContentItem> fetchYouTube(String query, UserGoal goal) {
        if (youtubeApiKey == null || youtubeApiKey.isBlank()) {
            log.warn("YOUTUBE_API_KEY is not set. Skipping YouTube recommendations.");
            return Collections.emptyList();
        }

        try {
            log.info("Fetching YouTube videos for query: '{}'", query);
            String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);

            Map<String, Object> response = webClient.get()
                    .uri("https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=10&q="
                            + encodedQuery + "&key=" + youtubeApiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || !response.containsKey("items")) {
                log.warn("YouTube returned no items");
                return Collections.emptyList();
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            List<ContentItem> results = new ArrayList<>();
            List<String> videoIds = new ArrayList<>();
            Map<String, ContentItem> itemMap = new HashMap<>();

            for (Map<String, Object> ytItem : items) {
                Map<String, Object> idObj = (Map<String, Object>) ytItem.get("id");
                if (idObj == null) continue;
                String videoId = (String) idObj.get("videoId");
                if (videoId == null) continue;

                Map<String, Object> snippet = (Map<String, Object>) ytItem.get("snippet");
                if (snippet == null) continue;

                ContentItem item = new ContentItem();
                item.setId("yt-" + videoId);
                item.setTitle((String) snippet.get("title"));
                item.setUrl("https://www.youtube.com/watch?v=" + videoId);
                item.setDescription((String) snippet.get("description"));
                item.setSource("youtube");
                item.setFormat("video");
                item.setDifficulty(goal.getSkillLevel() != null ? goal.getSkillLevel() : "beginner");

                Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                if (thumbnails != null) {
                    Map<String, Object> high = (Map<String, Object>) thumbnails.get("high");
                    if (high != null) item.setThumbnailUrl((String) high.get("url"));
                }

                item.setTags(new ArrayList<>(QueryBuilder.extractKeywords((String) snippet.get("title"))));
                item.setPopularityScore(75);

                results.add(item);
                videoIds.add(videoId);
                itemMap.put(videoId, item);
            }

            if (!videoIds.isEmpty()) {
                try {
                    String idsJoined = String.join(",", videoIds);
                    Map<String, Object> statsResponse = webClient.get()
                            .uri("https://www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics&id="
                                    + idsJoined + "&key=" + youtubeApiKey)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .timeout(Duration.ofSeconds(5))
                            .block();

                    if (statsResponse != null && statsResponse.containsKey("items")) {
                        List<Map<String, Object>> statItems = (List<Map<String, Object>>) statsResponse.get("items");
                        for (Map<String, Object> statItem : statItems) {
                            String vidId = (String) statItem.get("id");
                            ContentItem mappedItem = itemMap.get(vidId);
                            if (mappedItem != null) {
                                Map<String, Object> contentDetails = (Map<String, Object>) statItem.get("contentDetails");
                                if (contentDetails != null && contentDetails.get("duration") != null) {
                                    String isoDuration = (String) contentDetails.get("duration");
                                    mappedItem.setDurationMinutes(parseIsoDuration(isoDuration));
                                }

                                Map<String, Object> statistics = (Map<String, Object>) statItem.get("statistics");
                                if (statistics != null && statistics.get("viewCount") != null) {
                                    long views = Long.parseLong((String) statistics.get("viewCount"));
                                    mappedItem.setPopularityScore(normalizePopularity((int) Math.min(views, Integer.MAX_VALUE), 1000000));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch YouTube stats/duration: {}", e.getMessage());
                }
            }

            log.info("YouTube returned {} items", results.size());
            return results;

        } catch (Exception e) {
            log.error("YouTube API failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ======================== ENTERTAINMENT (BALANCED CONTENT) ========================

    @SuppressWarnings("unchecked")
    private List<ContentItem> fetchEntertainment(String query, UserGoal goal) {
        if (youtubeApiKey == null || youtubeApiKey.isBlank()) {
            return Collections.emptyList();
        }

        try {
            String entertainmentQuery = query + " funny";
            log.info("Fetching entertainment content for query: '{}'", entertainmentQuery);
            String encodedQuery = java.net.URLEncoder.encode(entertainmentQuery, java.nio.charset.StandardCharsets.UTF_8);

            Map<String, Object> response = webClient.get()
                    .uri("https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=3&q="
                            + encodedQuery + "&key=" + youtubeApiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || !response.containsKey("items")) {
                return Collections.emptyList();
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            List<ContentItem> results = new ArrayList<>();

            for (Map<String, Object> ytItem : items) {
                Map<String, Object> idObj = (Map<String, Object>) ytItem.get("id");
                if (idObj == null) continue;
                String videoId = (String) idObj.get("videoId");
                if (videoId == null) continue;

                Map<String, Object> snippet = (Map<String, Object>) ytItem.get("snippet");
                if (snippet == null) continue;

                ContentItem item = new ContentItem();
                item.setId("ent-" + videoId);
                item.setTitle((String) snippet.get("title"));
                item.setUrl("https://www.youtube.com/watch?v=" + videoId);
                item.setDescription((String) snippet.get("description"));
                item.setSource("youtube");
                item.setFormat("entertainment");
                item.setDifficulty(goal.getSkillLevel() != null ? goal.getSkillLevel() : "beginner");

                Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                if (thumbnails != null) {
                    Map<String, Object> high = (Map<String, Object>) thumbnails.get("high");
                    if (high != null) item.setThumbnailUrl((String) high.get("url"));
                }

                List<String> tags = new ArrayList<>(QueryBuilder.extractKeywords((String) snippet.get("title")));
                tags.add("entertainment");
                item.setTags(tags);
                item.setPopularityScore(70);

                results.add(item);
            }

            log.info("Entertainment returned {} items", results.size());
            return results;

        } catch (Exception e) {
            log.error("Entertainment fetch failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Integer parseIsoDuration(String isoDuration) {
        if (isoDuration == null || isoDuration.isBlank()) return null;
        try {
            return (int) Duration.parse(isoDuration).toMinutes();
        } catch (Exception e) {
            return null;
        }
    }

    // ======================== HELPERS ========================

    private int normalizePopularity(int rawCount, int maxExpected) {
        if (rawCount <= 0) return 0;
        if (rawCount >= maxExpected) return 100;
        return (int) Math.round((double) rawCount / maxExpected * 100);
    }

    // ======================== CACHE ========================

    private static class CacheEntry {
        final List<ContentItem> items;
        final Instant createdAt;

        CacheEntry(List<ContentItem> items) {
            this.items = items;
            this.createdAt = Instant.now();
        }

        boolean isExpired() {
            return Instant.now().isAfter(createdAt.plus(CACHE_TTL));
        }
    }
}