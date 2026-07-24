# 🔥 Forge — AI-Powered Goal-Driven Content Feed

> Built at **Nerds Hack Days, Lucknow** — a 2-day hackathon project.

Forge replaces engagement-optimized content feeds with a **goal-driven learning engine**. Tell Forge what you want to learn, and it curates content from YouTube, Reddit, GitHub, and LinkedIn — ranked by how well each piece fits your learning goal, not by how addictive it is.

**The pitch:** Same feed format, different engine. Every recommended item comes with a clear _"Why this was recommended"_ explanation tied to your specific goal.

---

## 🖥️ Frontend Stack

| Technology | Purpose |
|------------|---------|
| React 18 | UI framework |
| Vite | Build tool & dev server |
| Tailwind CSS v4 | Utility-first styling with custom theme |
| React Router v6 | Client-side routing |
| Axios | HTTP client with JWT interceptors |
| Recharts | Dashboard charts |

## 📁 Project Structure

```
src/
├── components/
│   ├── FeedCard.jsx         # Content card with recommendation display
│   ├── Navbar.jsx           # Top navigation bar
│   ├── ProtectedRoute.jsx   # Auth guard for routes
│   └── ReflectionModal.jsx  # Post-completion check-in modal
├── contexts/
│   └── AuthContext.jsx      # JWT auth state management
├── lib/
│   └── api.js               # Axios instance with token interceptor
├── pages/
│   ├── AuthPage.jsx         # Login / Signup toggle page
│   ├── GoalSetupPage.jsx    # Onboarding goal configuration
│   ├── FeedPage.jsx         # Core AI-curated content feed
│   └── DashboardPage.jsx    # Progress stats & streak tracking
├── App.jsx                  # Router & layout
├── main.jsx                 # Entry point
└── index.css                # Global styles & Tailwind theme
```

## 🚀 Getting Started

### Prerequisites
- Node.js 18+ and npm

### Setup

```bash
# Clone the repo
git clone https://github.com/Aditya-dxt/Forge-Nerd-Hackathon.git
cd Forge-Nerd-Hackathon

# Install dependencies
npm install

# Configure the backend API URL
cp .env.example .env
# Edit .env to point to your backend server:
# VITE_API_BASE_URL=http://localhost:5000
```

### Run Development Server

```bash
npm run dev
```

The app will start at `http://localhost:5173`.

### Build for Production

```bash
npm run build
npm run preview
```

## 🔌 Backend API

The frontend expects a backend server running at the URL specified in `VITE_API_BASE_URL`. The following endpoints are consumed:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Create account (email, password, name) |
| POST | `/api/auth/login` | Login (email, password) → JWT |
| POST | `/api/goals` | Create a learning goal |
| GET | `/api/goals/active` | Check for active goals |
| PUT | `/api/goals/:id` | Update a goal |
| GET | `/api/feed` | Get ranked content with "why recommended" |
| POST | `/api/interactions` | Log clicked/completed/skipped |
| POST | `/api/reflections` | Submit reflection check-in |
| GET | `/api/dashboard/summary` | Get streak, time, completions |

## 🎨 Design Philosophy

- **Anti-engagement-bait**: Calm, Notion/Linear-inspired aesthetic
- **Goal transparency**: Every recommendation shows _why_ it was chosen
- **Reflection-first**: Completing content triggers a self-check-in
- **Progress visibility**: Dashboard tracks streaks and learning time

## 👥 Team

Built with ❤️ at Nerds Hack Days, Lucknow.
