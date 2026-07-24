import React from 'react';

const sourceIcons = {
  youtube: (
    <svg className="w-4 h-4 text-[#FF0000]" viewBox="0 0 24 24" fill="currentColor">
      <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
    </svg>
  ),
  reddit: (
    <svg className="w-4 h-4 text-[#FF4500]" viewBox="0 0 24 24" fill="currentColor">
      <path d="M24 11.5c0-1.65-1.35-3-3-3-.96 0-1.86.48-2.42 1.24-1.64-1-3.75-1.64-6.07-1.72.08-1.1.4-3.05 1.52-3.7.72-.4 1.73-.24 3 .5C17.2 6.3 18.46 7.5 20 7.5c1.65 0 3-1.35 3-3s-1.35-3-3-3c-1.38 0-2.54.94-2.88 2.22-1.43-.72-2.64-.8-3.6-.25-1.64.94-1.95 3.47-2 4.55-2.33.08-4.45.7-6.1 1.72C4.86 8.98 3.96 8.5 3 8.5c-1.65 0-3 1.35-3 3 0 1.32.84 2.44 2.05 2.84-.03.22-.05.44-.05.66 0 3.86 4.5 7 10 7s10-3.14 10-7c0-.22-.02-.44-.05-.66 1.2-.4 2.05-1.54 2.05-2.84zM2.3 11.5c0-.94.76-1.7 1.7-1.7.6 0 1.14.32 1.45.82-1.8.84-3.15 2.05-3.15 3.58 0 .15.02.3.06.45-.63-.6-1.06-1.46-1.06-2.43V11.5zm16 5.5c-1.12 1.12-3.1 1.5-6.3 1.5-3.2 0-5.18-.38-6.3-1.5-.42-.42-.42-1.1 0-1.5.42-.42 1.1-.42 1.5 0 .8.8 2.2 1 4.8 1 2.6 0 4-.2 4.8-1 .42-.42 1.1-.42 1.5 0 .42.42.42 1.1 0 1.5zm-5.3-2.5c0 .83-.67 1.5-1.5 1.5s-1.5-.67-1.5-1.5.67-1.5 1.5-1.5 1.5.67 1.5 1.5zm6.5 2.87c0-1.53-1.35-2.74-3.15-3.58.3.5.85.82 1.45.82.94 0 1.7.76 1.7 1.7v.6c0 .97-.43 1.83-1.06 2.43.04-.15.06-.3.06-.45zM17.5 13c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5 1.5-.67 1.5-1.5-.67-1.5-1.5-1.5z"/>
    </svg>
  ),
  github: (
    <svg className="w-4 h-4 text-[#181717]" viewBox="0 0 24 24" fill="currentColor">
      <path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/>
    </svg>
  ),
  linkedin: (
    <svg className="w-4 h-4 text-[#0A66C2]" viewBox="0 0 24 24" fill="currentColor">
      <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
    </svg>
  ),
  default: (
    <svg className="w-4 h-4 text-text-secondary" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <circle cx="12" cy="12" r="10" />
      <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
      <path d="M2 12h20" />
    </svg>
  )
};

const placeholderColors = {
  youtube: 'bg-red-50',
  reddit: 'bg-orange-50',
  github: 'bg-gray-100',
  linkedin: 'bg-blue-50',
  default: 'bg-surface-secondary'
};

const FeedCard = ({ item, onStart, onComplete, onSkip }) => {
  const {
    title,
    source,
    format,
    duration,
    thumbnail,
    url,
    why_recommended
  } = item;

  const Icon = sourceIcons[source] || sourceIcons.default;
  const placeholderClass = placeholderColors[source] || placeholderColors.default;

  return (
    <div className="flex flex-col bg-surface border border-border-light rounded-xl overflow-hidden shadow-card hover:shadow-card-hover transition-all duration-300 animate-fade-in group h-full">
      {/* Thumbnail */}
      <div className={`w-full h-40 ${!thumbnail ? placeholderClass : 'bg-surface-secondary'} relative overflow-hidden flex-shrink-0`}>
        {thumbnail ? (
          <img src={thumbnail} alt={title} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
        ) : (
          <div className="absolute inset-0 flex items-center justify-center">
            {Icon}
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-5 flex flex-col flex-grow">
        {/* Meta Header */}
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <div className="flex items-center justify-center bg-surface-secondary w-7 h-7 rounded-full">
              {Icon}
            </div>
            <span className="text-xs font-medium text-text-secondary capitalize px-2 py-1 bg-surface-secondary rounded-full">
              {format}
            </span>
          </div>
          {duration && (
            <span className="text-xs font-medium text-text-tertiary">
              {duration}
            </span>
          )}
        </div>

        {/* Title */}
        <h3 className="font-semibold text-text-primary text-lg leading-tight mb-4 line-clamp-2">
          {title}
        </h3>

        {/* Recommendation Block */}
        <div className="mt-auto mb-5">
          <div className="flex items-center gap-1.5 mb-2">
            <svg className="w-3.5 h-3.5 text-accent-dark" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/>
            </svg>
            <span className="text-xs font-semibold text-accent-dark tracking-wide uppercase">Why this was recommended</span>
          </div>
          <div className="bg-accent-bg border border-accent/20 rounded-lg p-3 text-sm text-accent-dark font-medium leading-relaxed">
            {why_recommended}
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-3 pt-4 border-t border-border-light">
          <button
            onClick={() => onStart(item)}
            className="flex-1 bg-accent text-white py-2 px-4 rounded-lg text-sm font-medium hover:bg-accent/90 transition-colors focus:ring-2 focus:ring-accent/20 outline-none"
          >
            Start
          </button>
          <button
            onClick={() => onComplete(item)}
            className="flex-1 bg-transparent border border-accent text-accent-dark py-2 px-4 rounded-lg text-sm font-medium hover:bg-accent-bg transition-colors focus:ring-2 focus:ring-accent/20 outline-none"
          >
            Complete
          </button>
          <button
            onClick={() => onSkip(item)}
            className="text-text-tertiary hover:text-text-secondary py-2 px-3 text-sm font-medium transition-colors"
            title="Skip this item"
          >
            Skip
          </button>
        </div>
      </div>
    </div>
  );
};

export default FeedCard;
