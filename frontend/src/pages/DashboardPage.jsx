import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../lib/api';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  Cell,
} from 'recharts';

/**
 * Format minutes into human readable duration string (e.g., 2h 45m or 45m)
 */
function formatTimeSpent(minutes) {
  if (minutes == null || isNaN(minutes) || minutes <= 0) return '0m';
  const hrs = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hrs === 0) return `${mins}m`;
  if (mins === 0) return `${hrs}h`;
  return `${hrs}h ${mins}m`;
}

/**
 * Custom Recharts tooltip with Notion/Linear design styling
 */
function CustomTooltip({ active, payload, label }) {
  if (active && payload && payload.length) {
    return (
      <div className="bg-white border border-border-light shadow-md rounded-lg px-3.5 py-2.5 text-xs">
        <p className="font-semibold text-text-primary mb-1">{label}</p>
        <div className="flex items-center gap-2 text-text-secondary">
          <span className="w-2.5 h-2.5 rounded-full bg-accent inline-block" />
          <span>
            Completed: <strong className="text-text-primary font-bold">{payload[0].value}</strong> items
          </span>
        </div>
      </div>
    );
  }
  return null;
}

export default function DashboardPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Fetch active goal to get goalId
      const goalsRes = await api.get('/api/goals/active');
      const goalId = goalsRes.data[goalsRes.data.length - 1]?.id;

      const res = await api.get(`/api/dashboard/summary?goalId=${goalId}`);
      
      // Map API response to UI expected shape
      setData({
        streak: res.data.currentStreak || 0,
        timeSpentThisWeek: res.data.totalTimeSpentEstimateMinutes || 0,
        itemsCompleted: res.data.totalItemsCompleted || 0,
        breakdown: [] // Backend doesn't support breakdown yet
      });
    } catch (err) {
      console.error('Failed to fetch dashboard summary:', err);
      setError(err.response?.data?.message || 'Failed to load progress stats. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  // Determine if overall dataset is empty
  const isEmptyData =
    !data ||
    (data.streak === 0 &&
      data.timeSpentThisWeek === 0 &&
      data.itemsCompleted === 0 &&
      (!data.breakdown || data.breakdown.length === 0));

  return (
    <div className="min-h-[calc(100vh-3.5rem)] bg-surface text-text-primary py-8 px-4 sm:px-6">
      <div className="max-w-5xl mx-auto space-y-8 animate-fade-in">
        {/* Page Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border-light pb-6">
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-text-primary">
              Your Progress
            </h1>
            <p className="text-sm text-text-secondary mt-1">
              Track your daily learning momentum, activity hours, and topic achievements.
            </p>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={fetchDashboardData}
              disabled={loading}
              className="px-3 py-1.5 text-xs font-medium text-text-secondary hover:text-text-primary bg-white border border-border-light rounded-lg shadow-xs hover:bg-surface-secondary transition-all cursor-pointer disabled:opacity-50 flex items-center gap-1.5"
              title="Refresh Stats"
            >
              <svg
                className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`}
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                />
              </svg>
              Refresh
            </button>
            <Link
              to="/feed"
              className="px-3.5 py-1.5 text-xs font-medium text-white bg-accent hover:bg-accent-dark rounded-lg transition-colors shadow-xs"
            >
              Explore Feed &rarr;
            </Link>
          </div>
        </div>

        {/* Error Notification */}
        {error && (
          <div className="p-4 bg-danger-bg border border-danger/20 rounded-xl text-sm text-danger flex items-center justify-between">
            <span>{error}</span>
            <button
              onClick={fetchDashboardData}
              className="font-semibold underline hover:no-underline text-xs cursor-pointer"
            >
              Retry
            </button>
          </div>
        )}

        {/* Skeleton Loading State */}
        {loading ? (
          <div className="space-y-8">
            {/* Stat Cards Skeleton */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="bg-white rounded-xl border border-border-light p-6 shadow-card space-y-4"
                >
                  <div className="flex items-center justify-between">
                    <div className="h-4 w-28 skeleton rounded" />
                    <div className="w-8 h-8 skeleton rounded-lg" />
                  </div>
                  <div className="h-8 w-20 skeleton rounded" />
                  <div className="h-3 w-36 skeleton rounded" />
                </div>
              ))}
            </div>

            {/* Chart Skeleton */}
            <div className="bg-white rounded-xl border border-border-light p-6 shadow-card space-y-4">
              <div className="h-5 w-44 skeleton rounded" />
              <div className="h-3 w-64 skeleton rounded" />
              <div className="h-64 w-full skeleton rounded-lg mt-6" />
            </div>
          </div>
        ) : (
          <>
            {/* Stat Cards Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {/* Stat 1: 🔥 Current Streak */}
              <div className="bg-white rounded-xl border border-amber-200/80 p-6 shadow-card hover:shadow-card-hover transition-all relative overflow-hidden group">
                <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-amber-500 to-orange-500" />
                <div className="flex items-center justify-between mb-3">
                  <span className="text-sm font-medium text-text-secondary">Current Streak</span>
                  <span className="text-2xl group-hover:scale-110 transition-transform">🔥</span>
                </div>
                <div className="flex items-baseline gap-2">
                  <span className="text-3xl font-bold tracking-tight text-text-primary">
                    {data?.streak ?? 0}
                  </span>
                  <span className="text-sm font-medium text-text-secondary">days</span>
                </div>
                <p className="text-xs text-text-tertiary mt-2">
                  {data?.streak > 0
                    ? 'Keep learning daily to grow your streak!'
                    : 'Complete a learning item today to start a streak!'}
                </p>
              </div>

              {/* Stat 2: ⏱️ Time Spent This Week */}
              <div className="bg-white rounded-xl border border-border-light p-6 shadow-card hover:shadow-card-hover transition-all group">
                <div className="flex items-center justify-between mb-3">
                  <span className="text-sm font-medium text-text-secondary">Time This Week</span>
                  <span className="text-2xl group-hover:scale-110 transition-transform">⏱️</span>
                </div>
                <div className="flex items-baseline gap-2">
                  <span className="text-3xl font-bold tracking-tight text-text-primary">
                    {formatTimeSpent(data?.timeSpentThisWeek)}
                  </span>
                </div>
                <p className="text-xs text-text-tertiary mt-2">
                  Active learning & study time logged this week
                </p>
              </div>

              {/* Stat 3: ✅ Completed Items */}
              <div className="bg-white rounded-xl border border-border-light p-6 shadow-card hover:shadow-card-hover transition-all group">
                <div className="flex items-center justify-between mb-3">
                  <span className="text-sm font-medium text-text-secondary">Completed</span>
                  <span className="text-2xl group-hover:scale-110 transition-transform">✅</span>
                </div>
                <div className="flex items-baseline gap-2">
                  <span className="text-3xl font-bold tracking-tight text-text-primary">
                    {data?.itemsCompleted ?? 0}
                  </span>
                  <span className="text-sm font-medium text-text-secondary">items</span>
                </div>
                <p className="text-xs text-text-tertiary mt-2">
                  Total learning materials finished
                </p>
              </div>
            </div>

            {/* Breakdown Section */}
            <div className="bg-white rounded-xl border border-border-light p-6 shadow-card space-y-6">
              <div>
                <h2 className="text-lg font-semibold text-text-primary tracking-tight">
                  Learning Breakdown
                </h2>
                <p className="text-sm text-text-secondary mt-0.5">
                  Distribution of completed topics and resource types
                </p>
              </div>

              {data?.breakdown && Array.isArray(data.breakdown) && data.breakdown.length > 0 ? (
                <div className="h-72 w-full pt-2">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                      data={data.breakdown}
                      margin={{ top: 10, right: 10, left: -20, bottom: 25 }}
                    >
                      <CartesianGrid
                        strokeDasharray="3 3"
                        vertical={false}
                        stroke="var(--color-border-light)"
                      />
                      <XAxis
                        dataKey="name"
                        stroke="var(--color-text-tertiary)"
                        fontSize={12}
                        tickLine={false}
                        axisLine={{ stroke: 'var(--color-border-light)' }}
                        interval={0}
                        angle={-15}
                        textAnchor="end"
                      />
                      <YAxis
                        stroke="var(--color-text-tertiary)"
                        fontSize={12}
                        tickLine={false}
                        axisLine={{ stroke: 'var(--color-border-light)' }}
                        allowDecimals={false}
                      />
                      <Tooltip content={<CustomTooltip />} cursor={{ fill: 'var(--color-surface-secondary)' }} />
                      <Bar dataKey="count" fill="var(--color-accent)" radius={[6, 6, 0, 0]} maxBarSize={48}>
                        {data.breakdown.map((_, index) => (
                          <Cell
                            key={`cell-${index}`}
                            className="transition-opacity hover:opacity-80 cursor-pointer"
                          />
                        ))}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <div className="py-12 px-4 text-center bg-surface-secondary/50 rounded-lg border border-dashed border-border-light">
                  <div className="w-12 h-12 bg-accent-bg text-accent rounded-full flex items-center justify-center mx-auto mb-3">
                    <svg
                      className="w-6 h-6"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                      strokeWidth={1.5}
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        d="M9 19v-6a2 2 0 012-2h2a2 2 0 012 2v6m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                      />
                    </svg>
                  </div>
                  <h3 className="text-sm font-semibold text-text-primary mb-1">
                    No breakdown data yet
                  </h3>
                  <p className="text-xs text-text-secondary max-w-sm mx-auto">
                    Start reading and completing learning items in your feed to see your topic distribution here.
                  </p>
                </div>
              )}
            </div>

            {/* Global Empty State (If user has 0 stats across the board) */}
            {isEmptyData && (
              <div className="bg-accent-bg border border-accent-light rounded-xl p-6 text-center space-y-3">
                <h3 className="text-sm font-semibold text-accent-dark">
                  Welcome to your Forge Dashboard!
                </h3>
                <p className="text-xs text-text-secondary max-w-md mx-auto">
                  You haven't completed any items yet this week. Dive into your personalized feed to start building your daily learning streak!
                </p>
                <div className="pt-2">
                  <Link
                    to="/feed"
                    className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-accent hover:bg-accent-dark rounded-lg transition-colors shadow-xs"
                  >
                    Go to Feed
                  </Link>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
