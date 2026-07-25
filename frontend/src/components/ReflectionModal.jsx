import { useState, useEffect } from 'react';
import api from '../lib/api';

export default function ReflectionModal({ item, onClose }) {
  const [understood, setUnderstood] = useState(true);
  const [note, setNote] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Close modal on Escape key press
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        onClose?.();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  if (!item) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // Fetch active goal to get goalId as required by API
      const goalsRes = await api.get('/api/goals/active');
      const goalId = goalsRes.data[goalsRes.data.length - 1]?.id;

      await api.post('/api/reflections', {
        goalId,
        understood,
        note: note.trim(),
      });
      onClose?.();
    } catch (err) {
      console.error('Failed to submit reflection:', err);
      setError(err.response?.data?.message || 'Failed to submit reflection. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-fade-in"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-xl shadow-modal max-w-md w-full p-6 relative animate-slide-up"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Close/X Button */}
        <button
          type="button"
          onClick={onClose}
          className="absolute top-4 right-4 text-text-tertiary hover:text-text-primary p-1 rounded-lg transition-colors cursor-pointer"
          aria-label="Close modal"
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>

        {/* Modal Header */}
        <div className="mb-5 pr-6">
          <span className="inline-block px-2.5 py-1 rounded-full bg-accent-bg text-accent-dark text-xs font-semibold uppercase tracking-wider mb-2">
            Reflection Check-In
          </span>
          <h3 className="text-lg font-semibold text-text-primary leading-snug line-clamp-2">
            {item.title || item.name || 'Untitled Item'}
          </h3>
        </div>

        {/* Reflection Form */}
        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Question 1: Did you understand this? */}
          <div>
            <label className="block text-sm font-medium text-text-primary mb-2.5">
              Did you understand this?
            </label>
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={() => setUnderstood(true)}
                className={`flex-1 py-2.5 px-4 rounded-full text-sm font-medium transition-all cursor-pointer flex items-center justify-center gap-2 ${
                  understood === true
                    ? 'bg-accent text-white'
                    : 'bg-transparent border border-border text-text-secondary hover:text-text-primary hover:border-text-tertiary'
                }`}
              >
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <polyline points="20 6 9 17 4 12" />
                </svg>
                Yes
              </button>

              <button
                type="button"
                onClick={() => setUnderstood(false)}
                className={`flex-1 py-2.5 px-4 rounded-full text-sm font-medium transition-all cursor-pointer flex items-center justify-center gap-2 ${
                  understood === false
                    ? 'bg-accent text-white'
                    : 'bg-transparent border border-border text-text-secondary hover:text-text-primary hover:border-text-tertiary'
                }`}
              >
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
                No
              </button>
            </div>
          </div>

          {/* Optional Textarea: Any notes or reflections? */}
          <div>
            <label
              htmlFor="reflection-note"
              className="block text-sm font-medium text-text-primary mb-1.5"
            >
              Any notes or reflections?{' '}
              <span className="text-text-tertiary font-normal">(optional)</span>
            </label>
            <textarea
              id="reflection-note"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="What did you learn? Any key takeaways or questions?"
              rows={3}
              className="w-full rounded-lg border border-border bg-surface p-3 text-sm text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent resize-none"
            />
          </div>

          {/* Error display */}
          {error && (
            <div className="p-3 rounded-lg bg-red-50 border border-red-200 text-xs text-red-600">
              {error}
            </div>
          )}

          {/* Submit Button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-accent hover:bg-accent-dark text-white font-medium rounded-lg py-2.5 px-4 text-sm transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <svg
                  className="animate-spin h-4 w-4 text-white"
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
                  />
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  />
                </svg>
                <span>Submitting...</span>
              </>
            ) : (
              <span>Submit Reflection</span>
            )}
          </button>
        </form>
      </div>
    </div>
  );
}
