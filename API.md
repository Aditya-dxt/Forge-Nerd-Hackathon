\# GoalFlow API



Base URL: http://localhost:8080



\## POST /recommendations

Get scored/ranked content recommendations for a goal.



Request body: same shape as POST /goal



Response: 200 OK — array of results, sorted by matchScore descending

\[

&#x20; {

&#x20;   "item": {

&#x20;     "id": "6a63b3a7877042a4544e2496",

&#x20;     "source": "youtube",

&#x20;     "title": "Spring Boot REST API Crash Course",

&#x20;     "url": "https://youtube.com/watch?v=example1",

&#x20;     "description": "Fast-paced project-based intro to Spring Boot APIs",

&#x20;     "tags": \["java", "spring-boot", "backend", "rest-api"],

&#x20;     "format": "video",

&#x20;     "durationMinutes": 25,

&#x20;     "difficulty": "beginner",

&#x20;     "popularityScore": 85,

&#x20;     "thumbnailUrl": null

&#x20;   },

&#x20;   "matchScore": 64

&#x20; }

]



Notes:

\- durationMinutes and thumbnailUrl can be null (e.g. format "repo" or "discussion" items have no duration)

\- format is one of: "video", "repo", "discussion" (based on current seed data)

\- source is one of: "youtube", "github", "reddit"

