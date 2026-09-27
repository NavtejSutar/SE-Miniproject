import React, { useState } from 'react';
import { api } from '../api';
import { X, Lock, Mail, User, ArrowRight, AlertCircle, Loader2 } from 'lucide-react';

export default function AuthModal({ isOpen, onClose, initialMode = 'login', onSuccess }) {
  const [mode, setMode] = useState(initialMode); // 'login' | 'register'
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [username, setUsername] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (mode === 'login') {
        const res = await api.auth.login(email, password);
        onSuccess(res);
      } else {
        if (!username.trim()) throw new Error('Username is required');
        const res = await api.auth.register(username, email, password);
        onSuccess(res);
      }
      onClose();
    } catch (err) {
      setError(err.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="relative w-full max-w-md bg-[#121419] border border-[#272a33] p-8 shadow-2xl">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-5 right-5 text-gray-400 hover:text-white transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Header Tabs */}
        <div className="flex items-center gap-6 border-b border-[#262830] pb-4 mb-6">
          <button
            onClick={() => { setMode('login'); setError(''); }}
            className={`text-lg font-bold tracking-wider uppercase transition-colors ${
              mode === 'login' ? 'text-white border-b-2 border-[#ff3838] pb-1' : 'text-gray-500 hover:text-gray-300'
            }`}
          >
            Log In
          </button>
          <button
            onClick={() => { setMode('register'); setError(''); }}
            className={`text-lg font-bold tracking-wider uppercase transition-colors ${
              mode === 'register' ? 'text-white border-b-2 border-[#ff3838] pb-1' : 'text-gray-500 hover:text-gray-300'
            }`}
          >
            Create Account
          </button>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="mb-5 p-3 bg-red-950/50 border border-red-800/80 text-red-300 text-sm flex items-center gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {mode === 'register' && (
            <div>
              <label className="block text-xs uppercase font-semibold text-gray-400 mb-1.5 tracking-wider">
                Username
              </label>
              <div className="relative">
                <User className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" />
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="alex_dev"
                  className="w-full bg-[#181a20] border border-[#2a2d37] text-white pl-10 pr-4 py-2.5 text-sm focus:outline-none focus:border-[#ff3838] transition-colors"
                />
              </div>
            </div>
          )}

          <div>
            <label className="block text-xs uppercase font-semibold text-gray-400 mb-1.5 tracking-wider">
              Email Address
            </label>
            <div className="relative">
              <Mail className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@university.edu"
                className="w-full bg-[#181a20] border border-[#2a2d37] text-white pl-10 pr-4 py-2.5 text-sm focus:outline-none focus:border-[#ff3838] transition-colors"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs uppercase font-semibold text-gray-400 mb-1.5 tracking-wider">
              Password
            </label>
            <div className="relative">
              <Lock className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full bg-[#181a20] border border-[#2a2d37] text-white pl-10 pr-4 py-2.5 text-sm focus:outline-none focus:border-[#ff3838] transition-colors"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full mt-6 bg-[#ff3838] hover:bg-[#e02e2e] text-white font-bold py-3 px-4 text-sm tracking-wider uppercase flex items-center justify-center gap-2 transition-all cursor-pointer disabled:opacity-50"
          >
            {loading ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <>
                <span>{mode === 'login' ? 'Sign In' : 'Register Now'}</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        <div className="mt-6 text-center text-xs text-gray-500">
          Secured with stateless JWT authentication & bcrypt encryption.
        </div>
      </div>
    </div>
  );
}
