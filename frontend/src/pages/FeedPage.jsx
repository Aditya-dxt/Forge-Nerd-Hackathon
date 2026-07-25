import React, { useState, useEffect } from 'react';
import FeedCard from '../components/FeedCard';
import ReflectionModal from '../components/ReflectionModal';
import VideoPlayerModal from '../components/VideoPlayerModal';
import api from '../lib/api';

const SkeletonCard = () => (
  <div className="flex flex-col bg-surface border border-border-light rounded-xl overflow-hidden h-full shadow-sm">
    <div className="w-full h-40 bg-surface-secondary animate-pulse" />
    <div className="p-5 flex flex-col flex-grow gap-4">
      <div className="flex justify-between items-center">
        <div className="flex gap-2">
          <div className="w-7 h-7 bg-surface-secondary rounded-full animate-pulse" />
          <div className="w-16 h-6 bg-surface-secondary rounded-full animate-pulse" />
        </div>
        <div className="w-12 h-4 bg-surface-secondary rounded animate-pulse" />
      </div>
      <div className="space-y-2">
        <div className="w-full h-5 bg-surface-secondary rounded animate-pulse" />
        <div className="w-3/4 h-5 bg-surface-secondary rounded animate-pulse" />
      </div>
      <div className="mt-auto">
        <div className="w-32 h-4 bg-surface-secondary rounded animate-pulse mb-2" />
        <div className="w-full h-16 bg-surface-secondary rounded-lg animate-pulse" />
      </div>
      <div className="flex items-center gap-3 pt-4 border-t border-border-light">
        <div className="flex-1 h-9 bg-surface-secondary rounded-lg animate-pulse" />
        <div className="flex-1 h-9 bg-surface-secondary rounded-lg animate-pulse" />
        <div className="w-10 h-9 bg-surface-secondary rounded animate-pulse" />
      </div>
    </div>
  </div>
);

const EmptyState = () => (
  <div className="flex flex-col items-center justify-center py-20 px-4 text-center animate-fade-in">
    <div className="bg-surface-secondary p-6 rounded-full mb-6">
      <svg className="w-12 h-12 text-text-tertiary" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    </div>
    <h3 className="text-xl font-semibold text-text-primary mb-2">You're all caught up! 🎯</h3>
    <p className="text-text-secondary max-w-md">
      You've gone through all your recommended content for now. Head over to your dashboard to check your progress or set new goals.
    </p>
  </div>
);

const FeedPage = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reflectionItem, setReflectionItem] = useState(null);
  const [playingItem, setPlayingItem] = useState(null);

  const fetchFeed = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await api.get('/api/feed');
      const data = res.data;
      setItems(Array.isArray(data) ? data : data.items || data.feed || []);
    } catch (err) {
      console.error('Failed to fetch feed:', err);
      setError('Failed to load your feed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFeed();
  }, []);

  const getGoalId = async () => {
    const goalsRes = await api.get('/api/goals/active');
    return goalsRes.data[goalsRes.data.length - 1]?.id;
  };

  const handleStart = async (item) => {
    try {
      const goalId = await getGoalId();
      await api.post('/api/interactions', { goalId, contentId: item.id, action: 'CLICKED' });
    } catch (err) {
      console.error('Failed to record start interaction:', err);
    }

    if (item.source === 'youtube') {
      setPlayingItem(item);
    } else {
      window.open(item.url, '_blank', 'noopener,noreferrer');
    }
  };

  const handleComplete = async (item) => {
    try {
      const goalId = await getGoalId();
      await api.post('/api/interactions', { goalId, contentId: item.id, action: 'COMPLETED' });
    } catch (err) {
      console.error('Failed to record completion:', err);
    }
    setReflectionItem(item);
  };

  const handleAutoComplete = async (item) => {
    setPlayingItem(null);
    await handleComplete(item);
  };

  const handleReflectionClose = () => {
    if (reflectionItem) {
      setItems((prev) => prev.filter((i) => i.id !== reflectionItem.id));
    }
    setReflectionItem(null);
  };

  const handleSkip = async (item) => {
    try {
      setItems((prev) => prev.filter((i) => i.id !== item.id));
      const goalId = await getGoalId();
      await api.post('/api/interactions', { goalId, contentId: item.id, action: 'SKIPPED' });
    } catch (err) {
      console.error('Failed to record skip interaction:', err);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-text-primary tracking-tight">Your Feed</h1>
        <p className="text-text-secondary mt-1">Curated recommendations to help you reach your active goals.</p>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => <SkeletonCard key={i} />)}
        </div>
      ) : error ? (
        <div className="bg-red-50 border border-red-100 rounded-xl p-6 text-center">
          <p className="text-red-600 mb-4">{error}</p>
          <button
            onClick={fetchFeed}
            className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-red-700 transition-colors"
          >
            Retry
          </button>
        </div>
      ) : items.length === 0 ? (
        <EmptyState />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {items.map((item) => (
            <div key={item.id} className="animate-slide-up h-full">
              <FeedCard
                item={item}
                onStart={handleStart}
                onComplete={handleComplete}
                onSkip={handleSkip}
              />
            </div>
          ))}
        </div>
      )}

      {playingItem && (
        <VideoPlayerModal
          item={playingItem}
          onClose={() => setPlayingItem(null)}
          onAutoComplete={handleAutoComplete}
        />
      )}

      {reflectionItem && (
        <ReflectionModal
          item={reflectionItem}
          onClose={handleReflectionClose}
        />
      )}
    </div>
  );
};

export default FeedPage;