import React, { useState, useEffect, useRef } from 'react';
import { api } from '../api';
import { 
  Send, Plus, Trash2, Edit2, Check, X, MessageSquare, 
  Sparkles, FileText, ArrowRight, Loader2, Bot, User
} from 'lucide-react';

export default function ChatInterface({ onNavigateToLibrary }) {
  const [conversations, setConversations] = useState([]);
  const [activeConversationId, setActiveConversationId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [prompt, setPrompt] = useState('');
  const [loading, setLoading] = useState(false);
  const [editingConvId, setEditingConvId] = useState(null);
  const [editTitle, setEditTitle] = useState('');
  const messagesEndRef = useRef(null);

  // Load conversations on mount
  useEffect(() => {
    loadConversations();
  }, []);

  // Load messages when active conversation changes
  useEffect(() => {
    if (activeConversationId) {
      loadMessages(activeConversationId);
    } else {
      setMessages([]);
    }
  }, [activeConversationId]);

  // Scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const loadConversations = async () => {
    try {
      const list = await api.conversations.list();
      setConversations(list);
      if (list.length > 0 && !activeConversationId) {
        setActiveConversationId(list[0].ConversationId);
      }
    } catch (err) {
      console.error('Failed to load conversations', err);
    }
  };

  const loadMessages = async (id) => {
    try {
      const msgs = await api.conversations.getMessages(id);
      setMessages(msgs || []);
    } catch (err) {
      console.error('Failed to load messages', err);
    }
  };

  const handleCreateChat = async () => {
    try {
      const newChat = await api.conversations.create('New Study Session');
      setConversations(prev => [newChat, ...prev]);
      setActiveConversationId(newChat.ConversationId);
    } catch (err) {
      console.error('Failed to create conversation', err);
    }
  };

  const handleDeleteChat = async (e, id) => {
    e.stopPropagation();
    if (!window.confirm('Delete this conversation?')) return;
    try {
      await api.conversations.delete(id);
      setConversations(prev => prev.filter(c => c.ConversationId !== id));
      if (activeConversationId === id) {
        const remaining = conversations.filter(c => c.ConversationId !== id);
        setActiveConversationId(remaining.length > 0 ? remaining[0].ConversationId : null);
      }
    } catch (err) {
      console.error('Failed to delete conversation', err);
    }
  };

  const handleStartRename = (e, conv) => {
    e.stopPropagation();
    setEditingConvId(conv.ConversationId);
    setEditTitle(conv.Title);
  };

  const handleSaveRename = async (e, id) => {
    e.stopPropagation();
    if (!editTitle.trim()) return;
    try {
      await api.conversations.rename(id, editTitle.trim());
      setConversations(prev =>
        prev.map(c => (c.ConversationId === id ? { ...c, Title: editTitle.trim() } : c))
      );
      setEditingConvId(null);
    } catch (err) {
      console.error('Failed to rename conversation', err);
    }
  };

  const handleSendMessage = async (e) => {
    e?.preventDefault();
    if (!prompt.trim() || loading) return;

    let targetConvId = activeConversationId;

    // Auto-create chat if none exists
    if (!targetConvId) {
      try {
        const title = prompt.length > 25 ? prompt.substring(0, 25) + '...' : prompt;
        const newChat = await api.conversations.create(title);
        setConversations(prev => [newChat, ...prev]);
        setActiveConversationId(newChat.ConversationId);
        targetConvId = newChat.ConversationId;
      } catch (err) {
        console.error('Failed to create initial conversation', err);
        return;
      }
    }

    const currentPrompt = prompt;
    setPrompt('');
    
    // Optimistically append user message
    setMessages(prev => [
      ...prev,
      { role: 'USER', content: currentPrompt, createdAt: new Date().toISOString() }
    ]);
    setLoading(true);

    try {
      const response = await api.chat.send(targetConvId, currentPrompt);
      // Append assistant response with source citations
      setMessages(prev => [
        ...prev,
        {
          role: 'ASSISTANT',
          content: response.answer,
          sources: response.sources || [],
          createdAt: new Date().toISOString()
        }
      ]);
    } catch (err) {
      setMessages(prev => [
        ...prev,
        {
          role: 'ASSISTANT',
          content: `⚠️ Error: ${err.message || 'Unable to complete AI retrieval.'}`,
          createdAt: new Date().toISOString()
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const activeChat = conversations.find(c => c.ConversationId === activeConversationId);

  return (
    <div className="flex-1 flex overflow-hidden border-t border-[#26282f] bg-[#0c0d10] text-[#f5f6f8]">
      
      {/* Sidebar: Conversations List */}
      <aside className="w-72 md:w-80 border-r border-[#26282f] bg-[#0f1015] flex flex-col justify-between">
        <div>
          {/* Top Actions */}
          <div className="p-4 border-b border-[#26282f] flex items-center justify-between">
            <span className="text-xs font-mono uppercase tracking-widest text-gray-400 font-bold">
              Sessions ({conversations.length})
            </span>
            <button
              onClick={handleCreateChat}
              className="bg-[#ff3838] hover:bg-[#e02e2e] text-white p-1.5 transition-colors cursor-pointer"
              title="New Session"
            >
              <Plus className="w-4 h-4" />
            </button>
          </div>

          {/* List of Chats */}
          <div className="overflow-y-auto max-h-[calc(100vh-210px)] p-2 space-y-1">
            {conversations.map(conv => {
              const isActive = conv.ConversationId === activeConversationId;
              const isEditing = conv.ConversationId === editingConvId;

              return (
                <div
                  key={conv.ConversationId}
                  onClick={() => setActiveConversationId(conv.ConversationId)}
                  className={`group px-3 py-2.5 flex items-center justify-between text-xs cursor-pointer border transition-colors ${
                    isActive
                      ? 'bg-[#181a22] border-[#ff3838] text-white font-bold'
                      : 'bg-[#111318] border-transparent text-gray-400 hover:text-gray-200 hover:bg-[#14161d]'
                  }`}
                >
                  <div className="flex items-center gap-2.5 truncate flex-1">
                    <MessageSquare className={`w-3.5 h-3.5 flex-shrink-0 ${isActive ? 'text-[#ff3838]' : 'text-gray-500'}`} />
                    
                    {isEditing ? (
                      <input
                        type="text"
                        autoFocus
                        value={editTitle}
                        onClick={(e) => e.stopPropagation()}
                        onChange={(e) => setEditTitle(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleSaveRename(e, conv.ConversationId)}
                        className="bg-[#20232e] text-white px-2 py-0.5 text-xs outline-none border border-gray-600 w-full"
                      />
                    ) : (
                      <span className="truncate">{conv.Title || 'Untitled Session'}</span>
                    )}
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity ml-2">
                    {isEditing ? (
                      <button
                        onClick={(e) => handleSaveRename(e, conv.ConversationId)}
                        className="text-green-400 hover:text-green-300 p-1"
                      >
                        <Check className="w-3.5 h-3.5" />
                      </button>
                    ) : (
                      <button
                        onClick={(e) => handleStartRename(e, conv)}
                        className="text-gray-500 hover:text-white p-1"
                        title="Rename"
                      >
                        <Edit2 className="w-3 h-3" />
                      </button>
                    )}
                    <button
                      onClick={(e) => handleDeleteChat(e, conv.ConversationId)}
                      className="text-gray-500 hover:text-red-400 p-1"
                      title="Delete"
                    >
                      <Trash2 className="w-3 h-3" />
                    </button>
                  </div>
                </div>
              );
            })}

            {conversations.length === 0 && (
              <div className="p-6 text-center text-xs font-mono text-gray-500">
                No active sessions. Click "+" to start.
              </div>
            )}
          </div>
        </div>

        {/* Quick Link to Library */}
        <div className="p-3 border-t border-[#26282f] bg-[#0c0d11]">
          <button
            onClick={onNavigateToLibrary}
            className="w-full py-2 px-3 border border-[#2b2e3a] hover:border-gray-500 text-xs font-mono text-gray-300 flex items-center justify-between transition-colors"
          >
            <span className="flex items-center gap-2">
              <FileText className="w-3.5 h-3.5 text-[#ff3838]" /> Study Library
            </span>
            <ArrowRight className="w-3.5 h-3.5 text-gray-500" />
          </button>
        </div>
      </aside>

      {/* Main Chat Area */}
      <main className="flex-1 flex flex-col justify-between overflow-hidden bg-[#0c0d10]">
        
        {/* Chat Header */}
        <div className="px-6 py-3.5 border-b border-[#26282f] bg-[#0f1015] flex items-center justify-between text-xs font-mono">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
            <span className="text-white font-bold">{activeChat?.Title || 'StudyMate Grounded Q&A'}</span>
          </div>
          <span className="text-gray-400">Strict Source Grounding Active</span>
        </div>

        {/* Messages Scroll Area */}
        <div className="flex-1 overflow-y-auto p-4 md:p-8 space-y-6">
          {messages.length === 0 && !loading && (
            <div className="h-full flex flex-col items-center justify-center text-center max-w-lg mx-auto py-16">
              <div className="w-12 h-12 bg-[#171922] border border-[#2b2e3c] flex items-center justify-center text-[#ff3838] mb-4">
                <Sparkles className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-black uppercase tracking-wider text-white">Ask Anything From Your Notes</h3>
              <p className="text-xs text-gray-400 font-mono mt-1 mb-8 leading-relaxed">
                StudyMate retrieves relevant chunks from your uploaded documents and answers strictly using your verified material.
              </p>

              {/* Suggestions */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 w-full">
                {[
                  'What are the 4 conditions for deadlock?',
                  'Summarize the CPU scheduling algorithms.',
                  'Explain virtual memory paging with page faults.',
                  'List the key differences between TCP and UDP.'
                ].map((s, idx) => (
                  <button
                    key={idx}
                    onClick={() => { setPrompt(s); }}
                    className="p-3 bg-[#13151b] border border-[#262934] hover:border-[#ff3838] text-left text-xs font-mono text-gray-300 transition-colors"
                  >
                    "{s}"
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Render Messages */}
          {messages.map((msg, index) => {
            const isUser = msg.role === 'USER';

            return (
              <div
                key={index}
                className={`flex gap-3 md:gap-4 ${isUser ? 'justify-end' : 'justify-start'}`}
              >
                {!isUser && (
                  <div className="w-8 h-8 rounded-none bg-[#ff3838] text-white flex items-center justify-center font-bold text-xs flex-shrink-0">
                    AI
                  </div>
                )}

                <div
                  className={`max-w-2xl p-4 md:p-5 text-sm leading-relaxed ${
                    isUser
                      ? 'bg-[#181a23] border border-[#2f3342] text-white'
                      : 'bg-[#12141a] border border-[#262933] text-gray-200'
                  }`}
                >
                  {/* Message Content */}
                  <div className="whitespace-pre-wrap font-sans">{msg.content}</div>

                  {/* Grounded Source Citations */}
                  {msg.sources && msg.sources.length > 0 && (
                    <div className="mt-4 pt-3 border-t border-[#232630] space-y-2">
                      <div className="text-[10px] font-mono uppercase tracking-widest text-[#ff3838] font-bold">
                        Grounded Sources ({msg.sources.length}):
                      </div>
                      <div className="flex flex-wrap gap-2">
                        {msg.sources.map((src, i) => (
                          <span
                            key={i}
                            className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-[#1a1c24] border border-[#303444] text-[11px] font-mono text-gray-300"
                          >
                            <FileText className="w-3 h-3 text-[#ff3838]" />
                            <span>{src.fileName}</span>
                            <span className="text-[#ff3838] font-bold">p.{src.pageNumber}</span>
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  <div className="mt-2 text-[10px] font-mono text-gray-500 text-right">
                    {msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                  </div>
                </div>

                {isUser && (
                  <div className="w-8 h-8 rounded-none bg-[#272b38] text-white flex items-center justify-center font-bold text-xs flex-shrink-0">
                    <User className="w-4 h-4" />
                  </div>
                )}
              </div>
            );
          })}

          {/* Loading Indicator */}
          {loading && (
            <div className="flex gap-4 items-center text-xs font-mono text-gray-400">
              <div className="w-8 h-8 bg-[#ff3838] text-white flex items-center justify-center font-bold">
                <Loader2 className="w-4 h-4 animate-spin" />
              </div>
              <span>Searching PgVector & generating grounded answer...</span>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Input Bar */}
        <div className="p-4 md:p-6 border-t border-[#26282f] bg-[#0e0f14]">
          <form onSubmit={handleSendMessage} className="max-w-4xl mx-auto relative flex items-center">
            <input
              type="text"
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="Ask a question from your study notes..."
              className="w-full bg-[#15171e] border border-[#2b2e3b] text-white px-4 py-3.5 pr-14 text-sm font-sans focus:outline-none focus:border-[#ff3838] transition-colors"
            />
            <button
              type="submit"
              disabled={!prompt.trim() || loading}
              className="absolute right-2.5 p-2 bg-[#ff3838] hover:bg-[#e02e2e] text-white transition-colors disabled:opacity-40 cursor-pointer"
            >
              <Send className="w-4 h-4" />
            </button>
          </form>
          <div className="text-center text-[10px] font-mono text-gray-500 mt-2">
            Answers are restricted to authenticated user material. Zero external hallucination.
          </div>
        </div>

      </main>

    </div>
  );
}
