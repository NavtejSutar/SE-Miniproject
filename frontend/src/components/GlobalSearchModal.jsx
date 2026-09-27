import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { Search, X, FileText, MessageSquare, Loader2, ArrowRight } from 'lucide-react';

export default function GlobalSearchModal({ isOpen, onClose, onSelectConversation, onSelectDocument }) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!query.trim()) {
      setResults(null);
      return;
    }

    const timer = setTimeout(async () => {
      try {
        setLoading(true);
        const data = await api.search.query(query.trim());
        setResults(data);
      } catch (err) {
        console.error('Search failed', err);
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [query]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center pt-20 p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-2xl bg-[#12141a] border border-[#282b36] shadow-2xl overflow-hidden flex flex-col max-h-[80vh]">
        
        {/* Search Input Bar */}
        <div className="p-4 border-b border-[#262832] flex items-center gap-3 bg-[#151720]">
          <Search className="w-5 h-5 text-gray-400" />
          <input
            type="text"
            autoFocus
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search documents, lecture notes, or past conversations..."
            className="flex-1 bg-transparent text-white text-sm outline-none placeholder-gray-500 font-sans"
          />
          {loading && <Loader2 className="w-4 h-4 text-[#ff3838] animate-spin" />}
          <button onClick={onClose} className="text-gray-400 hover:text-white p-1">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Results Area */}
        <div className="p-4 overflow-y-auto space-y-4 flex-1">
          {!results && !loading && (
            <div className="py-12 text-center text-xs font-mono text-gray-500">
              Type at least one character to search your study knowledge base.
            </div>
          )}

          {results && (
            <>
              {/* Documents Results */}
              <div>
                <div className="text-[10px] font-mono uppercase tracking-widest text-[#ff3838] font-bold mb-2">
                  Documents ({results.documents?.length || 0})
                </div>
                {results.documents?.length > 0 ? (
                  <div className="space-y-1">
                    {results.documents.map(doc => (
                      <div
                        key={doc.documentId}
                        onClick={() => { onSelectDocument(doc); onClose(); }}
                        className="flex items-center justify-between p-2.5 bg-[#171a23] hover:bg-[#1f2230] border border-[#272b38] text-xs font-mono text-white cursor-pointer transition-colors"
                      >
                        <div className="flex items-center gap-2.5 truncate">
                          <FileText className="w-4 h-4 text-[#ff3838]" />
                          <span className="truncate">{doc.fileName}</span>
                        </div>
                        <span className="text-[10px] text-gray-500 font-sans uppercase tracking-wider">Open</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-xs text-gray-600 font-mono py-1">No matching documents.</div>
                )}
              </div>

              {/* Conversations Results */}
              <div className="pt-2">
                <div className="text-[10px] font-mono uppercase tracking-widest text-[#ff3838] font-bold mb-2">
                  Conversations ({results.conversations?.length || 0})
                </div>
                {results.conversations?.length > 0 ? (
                  <div className="space-y-1">
                    {results.conversations.map(conv => (
                      <div
                        key={conv.ConversationId}
                        onClick={() => { onSelectConversation(conv.ConversationId); onClose(); }}
                        className="flex items-center justify-between p-2.5 bg-[#171a23] hover:bg-[#1f2230] border border-[#272b38] text-xs font-mono text-white cursor-pointer transition-colors"
                      >
                        <div className="flex items-center gap-2.5 truncate">
                          <MessageSquare className="w-4 h-4 text-gray-400" />
                          <span className="truncate">{conv.Title}</span>
                        </div>
                        <ArrowRight className="w-3.5 h-3.5 text-gray-500" />
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-xs text-gray-600 font-mono py-1">No matching sessions.</div>
                )}
              </div>
            </>
          )}
        </div>

        {/* Footer */}
        <div className="p-2.5 bg-[#101116] border-t border-[#232630] text-center text-[10px] font-mono text-gray-500">
          Press ESC to dismiss
        </div>

      </div>
    </div>
  );
}
