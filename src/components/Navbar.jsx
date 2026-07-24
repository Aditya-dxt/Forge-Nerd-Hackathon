import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function Navbar() {
  const { logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!isAuthenticated) return null;

  return (
    <nav className="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-border-light">
      <div className="max-w-5xl mx-auto px-6 h-14 flex items-center justify-between">
        {/* Logo */}
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 bg-accent rounded-lg flex items-center justify-center">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 2L2 7l10 5 10-5-10-5z" />
              <path d="M2 17l10 5 10-5" />
              <path d="M2 12l10 5 10-5" />
            </svg>
          </div>
          <span className="font-semibold text-text-primary tracking-tight">Forge</span>
        </div>

        {/* Nav Links */}
        <div className="flex items-center gap-1">
          <NavLink
            to="/feed"
            className={({ isActive }) =>
              `px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-accent-bg text-accent-dark'
                  : 'text-text-secondary hover:text-text-primary hover:bg-surface-secondary'
              }`
            }
          >
            Feed
          </NavLink>
          <NavLink
            to="/dashboard"
            className={({ isActive }) =>
              `px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-accent-bg text-accent-dark'
                  : 'text-text-secondary hover:text-text-primary hover:bg-surface-secondary'
              }`
            }
          >
            Dashboard
          </NavLink>
          <NavLink
            to="/goals/setup"
            className={({ isActive }) =>
              `px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-accent-bg text-accent-dark'
                  : 'text-text-secondary hover:text-text-primary hover:bg-surface-secondary'
              }`
            }
          >
            New Goal
          </NavLink>
        </div>

        {/* Logout */}
        <button
          onClick={handleLogout}
          className="text-sm text-text-tertiary hover:text-text-primary transition-colors cursor-pointer"
        >
          Log out
        </button>
      </div>
    </nav>
  );
}
