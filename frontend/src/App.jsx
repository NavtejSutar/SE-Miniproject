import React, { useState, useEffect } from 'react';
import LandingPage from './components/LandingPage';
import Dashboard from './components/Dashboard';
import AuthModal from './components/AuthModal';
import { getStoredUser, api } from './api';

export default function App() {
  const [user, setUser] = useState(getStoredUser());
  const [currentView, setCurrentView] = useState('landing'); // 'landing' | 'dashboard'
  const [authModalOpen, setAuthModalOpen] = useState(false);
  const [authMode, setAuthMode] = useState('login');

  useEffect(() => {
    const handleUnauthorized = () => {
      setUser(null);
      setCurrentView('landing');
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  const handleAuthSuccess = (authData) => {
    setUser({ email: authData.email, username: authData.username });
    setCurrentView('dashboard');
  };

  const handleLogout = () => {
    api.auth.logout();
    setUser(null);
    setCurrentView('landing');
  };

  const handleGetStarted = () => {
    if (user) {
      setCurrentView('dashboard');
    } else {
      setAuthMode('register');
      setAuthModalOpen(true);
    }
  };

  const handleLoginClick = () => {
    if (user) {
      setCurrentView('dashboard');
    } else {
      setAuthMode('login');
      setAuthModalOpen(true);
    }
  };

  return (
    <>
      {currentView === 'landing' ? (
        <LandingPage
          user={user}
          onGetStarted={handleGetStarted}
          onLoginClick={handleLoginClick}
        />
      ) : (
        <Dashboard
          user={user}
          onLogout={handleLogout}
          onBackToLanding={() => setCurrentView('landing')}
        />
      )}

      <AuthModal
        isOpen={authModalOpen}
        initialMode={authMode}
        onClose={() => setAuthModalOpen(false)}
        onSuccess={handleAuthSuccess}
      />
    </>
  );
}
