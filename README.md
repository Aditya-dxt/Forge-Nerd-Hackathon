# Forge ⚒️ - Goal-Driven Learning Platform

Forge is an adaptive, goal-oriented learning feed built to help you master new skills efficiently. By entering a learning goal, Forge dynamically curates the best tutorials, repositories, and discussions from across the internet, organizing them into a personalized, actionable curriculum.

## 🚀 Features

- **Goal-Oriented Feed:** Simply type what you want to learn (e.g., "Master React Server Components"), and Forge builds a custom feed tailored to your skill level and available daily time.
- **Live Content Curation:** Instead of relying on static databases, Forge aggregates real-time learning resources from:
  - 🎥 **YouTube** (Video Tutorials)
  - 💻 **GitHub** (Top-starred Repositories)
  - 💬 **Reddit** (Community Discussions)
  - 📰 **Hacker News** (Tech Articles & Stories)
- **Smart Recommendations:** Uses keyword extraction, difficulty matching, and historical interaction data to score and rank content.
- **Progress Dashboard:** Track your learning momentum, time spent this week, completed items, and your daily streak.
- **Active Reflection:** Submit reflections after completing items to reinforce learning. If you struggle with a topic, the algorithm adapts and recommends simpler, shorter content.

## 🛠️ Tech Stack

**Frontend:**
- React 18 + Vite
- React Router DOM
- Tailwind CSS

**Backend:**
- Java 17
- Spring Boot 3
- Spring WebFlux (for concurrent external API calls)
- MongoDB Atlas (Cloud Database)

## ⚙️ Local Setup

### Prerequisites
- Node.js (v18+)
- Java JDK 17
- MongoDB Database (Local or Atlas)
- YouTube Data API v3 Key (Optional but recommended)

### 1. Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Configure environment variables. Create a `.env` file in the `backend` folder:
   ```env
   YOUTUBE_API_KEY=your_youtube_api_key_here
   ```
3. Configure your MongoDB connection in `backend/src/main/resources/application.properties`:
   ```properties
   spring.data.mongodb.uri=mongodb+srv://<user>:<password>@your-cluster.mongodb.net/?appName=Cluster0
   ```
4. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(The backend will start on `http://localhost:8080`)*

### 2. Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Configure environment variables. Create a `.env.local` file:
   ```env
   VITE_API_BASE_URL=http://localhost:8080
   ```
4. Start the Vite development server:
   ```bash
   npm run dev
   ```
   *(The frontend will start on `http://localhost:5173`)*

## 🧠 How the Recommendation Algorithm Works

The recommendation engine is built to adapt to the user's progress:
1. **Query Generation:** Extracts meaningful keywords from the user's goal, stripping out stopwords to build optimized queries for external APIs.
2. **Live Fetching:** Calls external APIs (YouTube, GitHub, HN, Reddit) in parallel with a 10-minute in-memory cache to prevent rate-limiting.
3. **Adaptive Scoring Engine (100 points max):**
   - **Keyword/Tag Overlap:** Up to 50 pts
   - **Difficulty Match:** Up to 20 pts
   - **Format Preference:** Up to 15 pts
   - **Duration Fit:** Up to 10 pts
   - **Popularity Tiebreaker:** Up to 5 pts
4. **Behavioral Adjustments:** The algorithm dynamically boosts formats/tags the user frequently completes and penalizes content types they often skip. If a recent reflection indicates poor understanding, the algorithm temporarily biases toward beginner-friendly, shorter content.

---
*Built for the Forge Nerd Hackathon.*
