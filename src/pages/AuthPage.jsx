import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import api from '../lib/api';

export default function AuthPage() {
  const navigate = useNavigate();
  const { login, signup } = useAuth();

  const [isSignup, setIsSignup] = useState(false);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const toggleMode = () => {
    setIsSignup((prev) => !prev);
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Please enter your email and password.');
      return;
    }

    if (isSignup && !name.trim()) {
      setError('Please enter your full name.');
      return;
    }

    setLoading(true);

    try {
      if (isSignup) {
        await signup(email, password, name.trim());
      } else {
        await login(email, password);
      }

      // Check for active goals to determine navigation target
      try {
        const goalsRes = await api.get('/api/goals/active');
        const data = goalsRes.data;

        let hasActiveGoals = false;
        if (Array.isArray(data)) {
          hasActiveGoals = data.length > 0;
        } else if (data && typeof data === 'object') {
          if (Array.isArray(data.goals)) {
            hasActiveGoals = data.goals.length > 0;
          } else if (typeof data.hasActive === 'boolean') {
            hasActiveGoals = data.hasActive;
          } else if (data.id || data.title || Object.keys(data).length > 0) {
            hasActiveGoals = true;
          }
        }

        if (hasActiveGoals) {
          navigate('/feed');
        } else {
          navigate('/goals/setup');
        }
      } catch (goalsErr) {
        // If checking active goals encounters 404 or empty response, direct to setup
        console.warn('Could not retrieve active goals, redirecting to goal setup:', goalsErr);
        navigate('/goals/setup');
      }
    } catch (err) {
      console.error('Authentication error:', err);
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        'Authentication failed. Please check your details and try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-surface flex flex-col justify-center items-center px-4 py-12 animate-fade-in">
      <div className="w-full max-w-md">
        {/* Branding Header */}
        <div className="flex flex-col items-center mb-8 text-center">
          <div className="flex items-center gap-2.5 mb-3">
            <div className="w-8 h-8 bg-accent rounded-lg flex items-center justify-center shadow-xs">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="white"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M12 2L2 7l10 5 10-5-10-5z" />
                <path d="M2 17l10 5 10-5" />
                <path d="M2 12l10 5 10-5" />
              </svg>
            </div>
            <span className="font-semibold text-xl text-text-primary tracking-tight">
              Forge
            </span>
          </div>
          <h1 className="text-xl font-bold text-text-primary tracking-tight">
            {isSignup ? 'Create your account' : 'Welcome back'}
          </h1>
          <p className="text-xs sm:text-sm text-text-secondary mt-1 max-w-xs">
            {isSignup
              ? 'Sign up to get started with your personalized learning goals'
              : 'Sign in to access your learning feed and goal progress'}
          </p>
        </div>

        {/* Auth Card */}
        <div className="bg-white border border-border-light rounded-2xl p-6 sm:p-8 shadow-card">
          <form onSubmit={handleSubmit} className="space-y-4">
            {isSignup && (
              <div className="animate-slide-up">
                <label
                  htmlFor="name"
                  className="block text-xs font-medium text-text-secondary mb-1.5"
                >
                  Full Name
                </label>
                <input
                  id="name"
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Jane Doe"
                  required={isSignup}
                  className="w-full px-3.5 py-2.5 rounded-lg border border-border bg-white text-text-primary placeholder:text-text-tertiary text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all"
                />
              </div>
            )}

            <div>
              <label
                htmlFor="email"
                className="block text-xs font-medium text-text-secondary mb-1.5"
              >
                Email address
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
                className="w-full px-3.5 py-2.5 rounded-lg border border-border bg-white text-text-primary placeholder:text-text-tertiary text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all"
              />
            </div>

            <div>
              <label
                htmlFor="password"
                className="block text-xs font-medium text-text-secondary mb-1.5"
              >
                Password
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                className="w-full px-3.5 py-2.5 rounded-lg border border-border bg-white text-text-primary placeholder:text-text-tertiary text-sm focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all"
              />
            </div>

            {/* Error Message */}
            {error && (
              <div className="p-3 rounded-lg bg-danger-bg border border-danger/30 text-danger text-xs sm:text-sm flex items-start gap-2.5 animate-slide-up">
                <svg
                  className="w-4 h-4 text-danger shrink-0 mt-0.5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                  />
                </svg>
                <span className="flex-1 font-medium">{error}</span>
              </div>
            )}

            {/* Submit Button */}
            <div className="pt-2">
              <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 px-4 bg-accent hover:bg-accent-dark text-white font-medium text-sm rounded-lg shadow-xs transition-colors duration-150 flex items-center justify-center gap-2 cursor-pointer focus:outline-none focus:ring-2 focus:ring-accent/40 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed"
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
                    <span>{isSignup ? 'Creating account...' : 'Signing in...'}</span>
                  </>
                ) : (
                  <span>{isSignup ? 'Sign up' : 'Sign in'}</span>
                )}
              </button>
            </div>
          </form>

          {/* Mode Toggle Footer */}
          <div className="mt-6 pt-5 border-t border-border-light text-center">
            <p className="text-xs text-text-secondary">
              {isSignup ? 'Already have an account?' : "Don't have an account?"}{' '}
              <button
                type="button"
                onClick={toggleMode}
                className="font-medium text-accent hover:text-accent-dark transition-colors cursor-pointer focus:outline-none underline-offset-2 hover:underline"
              >
                {isSignup ? 'Log in' : 'Sign up'}
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
