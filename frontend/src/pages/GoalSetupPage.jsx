import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../lib/api';

export default function GoalSetupPage() {
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [deadline, setDeadline] = useState('');
  const [skillLevel, setSkillLevel] = useState('intermediate');
  const [dailyTime, setDailyTime] = useState(1.5);
  const [preferredFormats, setPreferredFormats] = useState(['video', 'article']);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const formatOptions = [
    { id: 'video', label: 'video', icon: '🎥' },
    { id: 'article', label: 'article', icon: '📄' },
    { id: 'repo', label: 'repo', icon: '💻' },
    { id: 'discussion', label: 'discussion', icon: '💬' },
  ];

  const toggleFormat = (formatId) => {
    setPreferredFormats((prev) =>
      prev.includes(formatId)
        ? prev.filter((item) => item !== formatId)
        : [...prev, formatId]
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!title.trim()) {
      setError('Please enter a goal title.');
      return;
    }

    if (!deadline) {
      setError('Please select a target deadline.');
      return;
    }

    if (preferredFormats.length === 0) {
      setError('Please select at least one preferred format.');
      return;
    }

    setLoading(true);

    try {
      await api.post('/api/goals', {
        title: title.trim(),
        deadline,
        skillLevel,
        dailyTime: Number(dailyTime),
        preferredFormats,
      });

      navigate('/feed');
    } catch (err) {
      console.error('Failed to set goal:', err);
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to set up goal. Please check your connection and try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const todayStr = new Date().toISOString().split('T')[0];

  return (
    <div className="min-h-[calc(100vh-3.5rem)] bg-surface py-10 px-4 flex items-center justify-center animate-fade-in">
      <div className="w-full max-w-xl bg-white border border-border-light rounded-2xl p-6 sm:p-8 shadow-card">
        {/* Header */}
        <div className="mb-8">
          <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-accent-bg border border-accent/20 text-accent-dark text-xs font-medium mb-3">
            <span className="w-1.5 h-1.5 rounded-full bg-accent animate-pulse-soft"></span>
            Goal Onboarding
          </div>
          <h1 className="text-2xl font-bold text-text-primary tracking-tight">
            Set up your learning goal
          </h1>
          <p className="text-sm text-text-secondary mt-1.5 leading-relaxed">
            Define what you want to achieve, your timeline, and your learning preferences to build your personalized feed.
          </p>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="mb-6 p-4 rounded-xl bg-danger-bg border border-danger/30 text-danger text-sm flex items-start gap-3 animate-slide-up">
            <svg className="w-5 h-5 text-danger shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <div className="flex-1">
              <p className="font-medium">{error}</p>
            </div>
            <button
              type="button"
              onClick={() => setError('')}
              className="text-danger/70 hover:text-danger cursor-pointer"
              aria-label="Dismiss error"
            >
              ✕
            </button>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Goal Title */}
          <div>
            <label htmlFor="goal-title" className="block text-sm font-semibold text-text-primary mb-1">
              Goal Title <span className="text-danger">*</span>
            </label>
            <p className="text-xs text-text-tertiary mb-2">
              What topic, skill, or project do you want to focus on?
            </p>
            <input
              id="goal-title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g., Master React Server Components & Next.js"
              required
              className="w-full px-3.5 py-2.5 rounded-lg border border-border bg-white text-text-primary placeholder:text-text-tertiary text-sm focus:outline-none focus:ring-2 focus:ring-accent/30 focus:border-accent transition-all"
            />
          </div>

          {/* Target Deadline */}
          <div>
            <label htmlFor="goal-deadline" className="block text-sm font-semibold text-text-primary mb-1">
              Target Deadline <span className="text-danger">*</span>
            </label>
            <p className="text-xs text-text-tertiary mb-2">
              Set a realistic target completion date.
            </p>
            <input
              id="goal-deadline"
              type="date"
              value={deadline}
              min={todayStr}
              onChange={(e) => setDeadline(e.target.value)}
              required
              className="w-full px-3.5 py-2.5 rounded-lg border border-border bg-white text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-accent/30 focus:border-accent transition-all"
            />
          </div>

          {/* Skill Level - 3-Segment Toggle Bar */}
          <div>
            <label className="block text-sm font-semibold text-text-primary mb-1">
              Skill Level
            </label>
            <p className="text-xs text-text-tertiary mb-2.5">
              Select your current proficiency level.
            </p>
            <div className="grid grid-cols-3 p-1 rounded-xl bg-surface-secondary border border-border-light">
              {['beginner', 'intermediate', 'advanced'].map((level) => {
                const isSelected = skillLevel === level;
                return (
                  <button
                    key={level}
                    type="button"
                    onClick={() => setSkillLevel(level)}
                    className={`py-2 px-3 rounded-lg text-xs sm:text-sm capitalize font-medium transition-all duration-150 cursor-pointer ${
                      isSelected
                        ? 'bg-white text-text-primary shadow-xs font-semibold border border-border/40'
                        : 'text-text-secondary hover:text-text-primary hover:bg-white/50'
                    }`}
                  >
                    {level}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Daily Time Available - Range Slider */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label htmlFor="daily-time" className="text-sm font-semibold text-text-primary">
                Daily Time Available
              </label>
              <span className="px-2.5 py-0.5 rounded-full bg-accent-bg border border-accent/20 text-accent-dark text-xs font-semibold">
                {dailyTime} {Number(dailyTime) === 1 ? 'hour' : 'hours'} / day
              </span>
            </div>
            <p className="text-xs text-text-tertiary mb-3">
              How much time can you spend learning each day? (0.5h – 4.0h)
            </p>
            <div className="space-y-2">
              <input
                id="daily-time"
                type="range"
                min="0.5"
                max="4"
                step="0.5"
                value={dailyTime}
                onChange={(e) => setDailyTime(Number(e.target.value))}
                className="w-full h-2 bg-surface-tertiary rounded-lg appearance-none cursor-pointer accent-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
              />
              <div className="flex justify-between text-[11px] text-text-tertiary font-medium px-0.5">
                <span>0.5h</span>
                <span>1.0h</span>
                <span>1.5h</span>
                <span>2.0h</span>
                <span>2.5h</span>
                <span>3.0h</span>
                <span>3.5h</span>
                <span>4.0h</span>
              </div>
            </div>
          </div>

          {/* Preferred Formats - Multi-select Chips */}
          <div>
            <label className="block text-sm font-semibold text-text-primary mb-1">
              Preferred Formats
            </label>
            <p className="text-xs text-text-tertiary mb-3">
              Choose the types of learning materials you prefer.
            </p>
            <div className="flex flex-wrap gap-2.5">
              {formatOptions.map((fmt) => {
                const isSelected = preferredFormats.includes(fmt.id);
                return (
                  <button
                    key={fmt.id}
                    type="button"
                    onClick={() => toggleFormat(fmt.id)}
                    className={`inline-flex items-center gap-2 px-3.5 py-2 rounded-full text-xs sm:text-sm font-medium border transition-all duration-150 cursor-pointer ${
                      isSelected
                        ? 'bg-accent-bg border-accent text-accent-dark shadow-2xs'
                        : 'bg-surface border-border text-text-secondary hover:bg-surface-secondary hover:text-text-primary'
                    }`}
                  >
                    <span>{fmt.icon}</span>
                    <span>{fmt.label}</span>
                    {isSelected && (
                      <svg
                        className="w-3.5 h-3.5 text-accent shrink-0 stroke-[2.5]"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                      </svg>
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Submit Button */}
          <div className="pt-4">
            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl bg-accent hover:bg-accent-dark text-white font-medium text-sm transition-colors shadow-xs flex items-center justify-center gap-2 cursor-pointer border border-transparent focus:outline-none focus:ring-2 focus:ring-accent/40 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <svg
                    className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    ></circle>
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    ></path>
                  </svg>
                  <span>Setting up goal...</span>
                </>
              ) : (
                <>
                  <span>Create Goal & Continue</span>
                  <svg
                    className="w-4 h-4"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                  </svg>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
