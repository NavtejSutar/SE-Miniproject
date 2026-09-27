import React, { useState, useEffect } from 'react';
import ChatInterface from './ChatInterface';
import DocumentManager from './DocumentManager';
import SummarizerModal from './SummarizerModal';
import GlobalSearchModal from './GlobalSearchModal';
import { 
  MessageSquare, FileText, Search, LogOut, 
  ArrowLeft, User as UserIcon 
} from 'lucide-react';

export default function Dashboard({ user, onLogout, onBackToLanding }) {
  const [activeTab, setActiveTab] = useState('chat'); // 'chat' | 'library'
  const [summarizeDoc, setSummarizeDoc] = useState(null);
  const [searchOpen, setSearchOpen] = useState(false);

  // Global Ctrl+K shortcut
  useEffect(() => {
    const handleKeyDown = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        setSearchOpen(prev => !prev);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  return (
    <div className="h-screen h-[100dvh] bg-[#0c0d10] text-[#f5f6f8] flex flex-col font-sans selection:bg-[#ff3838] selection:text-white overflow-hidden">
      
      {/* Top Application Header (Completely Static - flex-shrink-0) */}
      <header className="flex-shrink-0 border-b border-[#26282f] bg-[#0f1015] px-6 py-3.5 flex items-center justify-between z-30 select-none">
        
        {/* Left: Brand & Landing Switcher */}
        <div className="flex items-center gap-6">
          <button
            onClick={onBackToLanding}
            className="flex items-center gap-2 text-xs font-mono uppercase tracking-wider text-gray-400 hover:text-white transition-colors cursor-pointer"
            title="Return to Home"
          >
            <ArrowLeft className="w-4 h-4 text-[#ff3838]" />
            <span className="hidden sm:inline">Landing</span>
          </button>

          <div className="h-4 w-[1px] bg-[#292c36]" />

          <div className="flex items-center gap-2 cursor-pointer" onClick={() => setActiveTab('chat')}>
            <div className="flex items-center justify-center w-6 h-6 bg-white text-black font-black text-xs">
              SM
            </div>
            <span className="font-black text-base uppercase tracking-wider text-white">StudyMate</span>
          </div>
        </div>

        {/* Center: Tabs (Chat Assistant & Document Library) */}
        <div className="flex items-center gap-1 bg-[#14161e] border border-[#262832] p-1">
          <button
            onClick={() => setActiveTab('chat')}
            className={`flex items-center gap-2 px-4 py-1.5 text-xs font-bold uppercase tracking-wider transition-colors cursor-pointer ${
              activeTab === 'chat'
                ? 'bg-[#ff3838] text-white'
                : 'text-gray-400 hover:text-white hover:bg-[#1b1e28]'
            }`}
          >
            <MessageSquare className="w-3.5 h-3.5" />
            <span>Chat Assistant</span>
          </button>

          <button
            onClick={() => setActiveTab('library')}
            className={`flex items-center gap-2 px-4 py-1.5 text-xs font-bold uppercase tracking-wider transition-colors cursor-pointer ${
              activeTab === 'library'
                ? 'bg-[#ff3838] text-white'
                : 'text-gray-400 hover:text-white hover:bg-[#1b1e28]'
            }`}
          >
            <FileText className="w-3.5 h-3.5" />
            <span>Library</span>
          </button>
        </div>

        {/* Right: Search, User Chip & Logout */}
        <div className="flex items-center gap-3">
          <button
            onClick={() => setSearchOpen(true)}
            className="flex items-center gap-2 px-3 py-1.5 bg-[#151720] border border-[#2b2e3b] text-gray-400 hover:text-white hover:border-gray-500 text-xs font-mono transition-colors cursor-pointer"
          >
            <Search className="w-3.5 h-3.5" />
            <span className="hidden lg:inline">Search...</span>
            <kbd className="hidden lg:inline bg-[#222530] text-[9px] px-1.5 py-0.5 text-gray-400">Ctrl+K</kbd>
          </button>

          <div className="flex items-center gap-2 pl-2 border-l border-[#262832]">
            <div className="w-7 h-7 bg-[#232734] border border-[#333748] flex items-center justify-center text-xs font-bold text-white uppercase">
              {user?.username ? user.username[0] : 'U'}
            </div>
            <span className="text-xs font-mono text-gray-300 hidden sm:inline">{user?.username}</span>

            <button
              onClick={onLogout}
              className="p-1.5 text-gray-500 hover:text-red-400 transition-colors cursor-pointer"
              title="Log Out"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>

      </header>

      {/* Main Content View (Strict flex-1 overflow-hidden min-h-0: Fits remaining viewport) */}
      <div className="flex-1 flex overflow-hidden min-h-0">
        {activeTab === 'chat' && (
          <ChatInterface onNavigateToLibrary={() => setActiveTab('library')} />
        )}

        {activeTab === 'library' && (
          <div className="flex-1 overflow-y-auto min-h-0">
            <DocumentManager
              onOpenSummarize={(doc) => setSummarizeDoc(doc)}
              onViewDocument={(doc) => setSummarizeDoc(doc)}
            />
          </div>
        )}
      </div>

      {/* Summarizer Modal */}
      <SummarizerModal
        isOpen={!!summarizeDoc}
        document={summarizeDoc}
        onClose={() => setSummarizeDoc(null)}
      />

      {/* Global Search Modal */}
      <GlobalSearchModal
        isOpen={searchOpen}
        onClose={() => setSearchOpen(false)}
        onSelectConversation={() => setActiveTab('chat')}
        onSelectDocument={(doc) => {
          setActiveTab('library');
          setSummarizeDoc(doc);
        }}
      />

    </div>
  );
}
