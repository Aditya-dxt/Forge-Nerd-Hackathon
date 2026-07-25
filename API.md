# GoalFlow API

Base URL: http://localhost:8080

---

## POST /goal

Save a learning goal.

**Request body:**

```json
{
  "goal": "Learn Spring Boot and backend development",
  "deadline": "2026-08-15",
  "skillLevel": "beginner",
  "dailyMinutes": 60,
  "preferredFormats": ["video", "repo"],
  "avoidTopics": ["frontend"]
}
```

**Response:** 200 OK — the saved goal object with its generated `id`.

```json
{
  "id": "669a1b2c3d4e5f6789012345",
  "goal": "Learn Spring Boot and backend development",
  "deadline": "2026-08-15",
  "skillLevel": "beginner",
  "dailyMinutes": 60,
  "preferredFormats": ["video", "repo"],
  "avoidTopics": ["frontend"]
}
```

---

## GET /goal

Get all saved goals.

**Response:** 200 OK — array of all UserGoal objects.

---

## POST /recommendations

Get scored/ranked content recommendations for a goal. Scoring is **adaptive**: it factors in past interactions and reflections to personalize results.

**Request body:** same shape as POST /goal (a UserGoal object, with `id` included if you want adaptive scoring)

```json
{
  "id": "669a1b2c3d4e5f6789012345",
  "goal": "Learn Spring Boot and backend development",
  "skillLevel": "beginner",
  "dailyMinutes": 60,
  "preferredFormats": ["video", "repo"]
}
```

**Response:** 200 OK — array of results, sorted by `matchScore` descending.

```json
[
  {
    "item": {
      "id": "6a63b3a7877042a4544e2496",
      "source": "youtube",
      "title": "Spring Boot REST API Crash Course",
      "url": "https://youtube.com/watch?v=example1",
      "description": "Fast-paced project-based intro to Spring Boot APIs",
      "tags": ["java", "spring-boot", "backend", "rest-api"],
      "format": "video",
      "durationMinutes": 25,
      "difficulty": "beginner",
      "popularityScore": 85,
      "thumbnailUrl": null
    },
    "matchScore": 84,
    "whyRecommended": "Matches your goal keywords: java, spring-boot, backend. Matches your beginner skill level. In your preferred format: video. Fits within your daily 60-minute window"
  }
]
```

**Adaptive scoring details:**
- Content the user has already COMPLETED or SKIPPED is excluded from results
- Formats and tags the user has completed before get a score boost
- Formats and difficulties the user tends to skip get a score penalty
- If the user's most recent reflection has `understood=false`, the system biases toward easier difficulty and shorter content
- The `whyRecommended` string explains the reasoning, e.g. "Adjusted to easier level based on your recent reflection" or "You've successfully completed 3 video items before"

**Notes:**
- `durationMinutes` and `thumbnailUrl` can be null (e.g. format "repo" or "discussion" items have no duration)
- `format` is one of: `"video"`, `"repo"`, `"discussion"`
- `source` is one of: `"youtube"`, `"github"`, `"reddit"`

---

## POST /interactions

Log a user interaction with a content item. Supports behavioral learning.

**Request body:**

```json
{
  "goalId": "669a1b2c3d4e5f6789012345",
  "contentId": "6a63b3a7877042a4544e2496",
  "contentTitle": "Spring Boot REST API Crash Course",
  "source": "youtube",
  "action": "COMPLETED"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `goalId` | string | ✅ | ID of the goal this interaction belongs to |
| `contentId` | string | one of contentId/contentTitle required | MongoDB ID of the content item |
| `contentTitle` | string | one of contentId/contentTitle required | Title of the content (fallback identifier) |
| `source` | string | optional | Source platform (youtube/reddit/github) |
| `action` | string | ✅ | One of: `SHOWN`, `CLICKED`, `COMPLETED`, `SKIPPED` |

**Response:** 201 Created — the saved Interaction object with `id` and `timestamp`.

```json
{
  "id": "669b2c3d4e5f67890123abcd",
  "goalId": "669a1b2c3d4e5f6789012345",
  "contentId": "6a63b3a7877042a4544e2496",
  "contentTitle": "Spring Boot REST API Crash Course",
  "source": "youtube",
  "action": "COMPLETED",
  "timestamp": "2026-07-25T09:00:00.000Z"
}
```

**Error responses:**
- `400 Bad Request` — missing `goalId`, invalid `action` value, or missing both `contentId` and `contentTitle`

---

## POST /reflections

Submit a post-completion reflection check-in. Drives adaptive difficulty in recommendations.

**Request body:**

```json
{
  "goalId": "669a1b2c3d4e5f6789012345",
  "understood": false,
  "note": "The async concepts were confusing, need simpler material"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `goalId` | string | ✅ | ID of the goal this reflection belongs to |
| `understood` | boolean | ✅ | Did the user understand the completed content? |
| `note` | string | optional | Free-text reflection note |

**Response:** 201 Created — the saved Reflection object with `id` and `timestamp`.

```json
{
  "id": "669c3d4e5f678901234abcde",
  "goalId": "669a1b2c3d4e5f6789012345",
  "understood": false,
  "note": "The async concepts were confusing, need simpler material",
  "timestamp": "2026-07-25T09:05:00.000Z"
}
```

**Effect on recommendations:** When `understood=false`, the next call to `POST /recommendations` for this goal will bias results toward easier difficulty and shorter-form content, with the `whyRecommended` string reflecting this adjustment.

**Error responses:**
- `400 Bad Request` — missing `goalId`

---

## GET /dashboard/summary

Get aggregated progress stats for a goal.

**Query parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `goalId` | string | ✅ | ID of the goal to summarize |

**Example:** `GET /dashboard/summary?goalId=669a1b2c3d4e5f6789012345`

**Response:** 200 OK

```json
{
  "totalItemsCompleted": 5,
  "totalItemsSkipped": 2,
  "currentStreak": 3,
  "totalTimeSpentEstimateMinutes": 145,
  "understoodRate": 80.0
}
```

| Field | Type | Description |
|-------|------|-------------|
| `totalItemsCompleted` | int | Count of COMPLETED interactions for this goal |
| `totalItemsSkipped` | int | Count of SKIPPED interactions for this goal |
| `currentStreak` | int | Consecutive days with at least one COMPLETED interaction (ending today or yesterday) |
| `totalTimeSpentEstimateMinutes` | int | Sum of `durationMinutes` for completed content items (estimates 10 min for items without a known duration) |
| `understoodRate` | double | Percentage of reflections where `understood=true` (0.0 if no reflections yet) |

**Error responses:**
- `400 Bad Request` — missing `goalId` query parameter
