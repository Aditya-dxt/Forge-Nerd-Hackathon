import React, { useEffect, useRef, useState } from 'react';

const extractVideoId = (url) => {
  try {
    const u = new URL(url);
    return u.searchParams.get('v');
  } catch {
    return null;
  }
};

const loadYouTubeAPI = () => {
  return new Promise((resolve) => {
    if (window.YT && window.YT.Player) {
      resolve(window.YT);
      return;
    }
    const existingScript = document.getElementById('youtube-iframe-api');
    if (!existingScript) {
      const tag = document.createElement('script');
      tag.id = 'youtube-iframe-api';
      tag.src = 'https://www.youtube.com/iframe_api';
      document.body.appendChild(tag);
    }
    window.onYouTubeIframeAPIReady = () => resolve(window.YT);
  });
};

const VideoPlayerModal = ({ item, onClose, onAutoComplete }) => {
  const playerRef = useRef(null);
  const playerInstance = useRef(null);
  const intervalRef = useRef(null);
  const [progress, setProgress] = useState(0);
  const [completedFired, setCompletedFired] = useState(false);

  const videoId = extractVideoId(item.url);

  useEffect(() => {
    let isMounted = true;

    loadYouTubeAPI().then((YT) => {
      if (!isMounted || !videoId) return;

      playerInstance.current = new YT.Player(playerRef.current, {
        videoId,
        playerVars: { autoplay: 1, rel: 0 },
        events: {
          onReady: () => {
            intervalRef.current = setInterval(() => {
              const player = playerInstance.current;
              if (!player || typeof player.getCurrentTime !== 'function') return;
              const current = player.getCurrentTime();
              const duration = player.getDuration();
              if (duration > 0) {
                const pct = (current / duration) * 100;
                setProgress(pct);
              }
            }, 2000);
          },
        },
      });
    });

    return () => {
      isMounted = false;
      if (intervalRef.current) clearInterval(intervalRef.current);
      if (playerInstance.current && playerInstance.current.destroy) {
        playerInstance.current.destroy();
      }
    };
  }, [videoId]);

  useEffect(() => {
    if (progress >= 90 && !completedFired) {
      setCompletedFired(true);
      onAutoComplete(item);
    }
  }, [progress, completedFired, item, onAutoComplete]);

  if (!videoId) {
    return null;
  }

  return (
    <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
      <div className="bg-surface rounded-xl overflow-hidden w-full max-w-3xl shadow-2xl">
        <div className="flex items-center justify-between p-4 border-b border-border-light">
          <h3 className="font-semibold text-text-primary truncate pr-4">{item.title}</h3>
          <button
            onClick={onClose}
            className="text-text-tertiary hover:text-text-primary text-xl leading-none px-2"
          >
            ×
          </button>
        </div>
        <div className="aspect-video w-full bg-black">
          <div ref={playerRef} className="w-full h-full" />
        </div>
        <div className="p-4">
          <div className="w-full h-2 bg-surface-secondary rounded-full overflow-hidden">
            <div
              className="h-full bg-accent transition-all duration-500"
              style={{ width: `${Math.min(progress, 100)}%` }}
            />
          </div>
          <p className="text-xs text-text-tertiary mt-2">
            {progress >= 90
              ? 'Marked as complete!'
              : `${Math.round(progress)}% watched — auto-completes at 90%`}
          </p>
        </div>
      </div>
    </div>
  );
};

export default VideoPlayerModal;
