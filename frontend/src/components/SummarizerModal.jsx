import React, { useState } from 'react';
import { api } from '../api';
import { X, Sparkles, Copy, Check, AlertCircle, Loader2 } from 'lucide-react';

export default function SummarizerModal({ isOpen, onClose, document }) {
  const [pageInput, setPageInput] = useState('');
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);

  if (!isOpen || !document) return null;

  const handleSummarize = async () => {
    try {
      setLoading(true);
      setError('');

      let pageNumbers = null;
      if (pageInput.trim()) {
        pageNumbers = pageInput
          .split(',')
          .map(s => parseInt(s.trim(), 10))
          .filter(n => !isNaN(n) && n > 0);
      }

      const res = await api.documents.summarize(document.documentId, pageNumbers);
      setSummary(res.summary);
    } catch (err) {
      setError(err.message || 'Failed to generate summary');
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = () => {
    if (!summary) return;
    navigator.clipboard.writeText(summary);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="relative w-full max-w-2xl bg-[#121419] border border-[#272a33] p-6 md:p-8 shadow-2xl flex flex-col max-h-[90vh]">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[#262830] pb-4 mb-4">
          <div className="flex items-center gap-2 text-white">
            <Sparkles className="w-5 h-5 text-[#ff3838]" />
            <h3 className="text-lg font-black uppercase tracking-wider">AI Document Summarizer</h3>
          </div>
          <button onClick={onClose} className="text-gray-400 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Target Document */}
        <div className="mb-4 text-xs font-mono text-gray-400 flex items-center justify-between bg-[#171922] p-2.5 border border-[#2a2d39]">
          <span className="truncate">Document: <strong className="text-white">{document.fileName}</strong></span>
          <span className="text-[#ff3838]">Ollama Powered</span>
        </div>

        {/* Page Filter Input */}
        <div className="mb-4">
          <label className="block text-xs font-bold uppercase tracking-wider text-gray-400 mb-1.5 font-mono">
            Specific Pages (Optional)
          </label>
          <div className="flex gap-2">
            <input
              type="text"
              value={pageInput}
              onChange={(e) => setPageInput(e.target.value)}
              placeholder="e.g. 1, 3, 5 (leave blank for entire document)"
              className="flex-1 bg-[#181a20] border border-[#2a2d37] text-white px-3 py-2 text-xs font-mono focus:outline-none focus:border-[#ff3838]"
            />
            <button
              onClick={handleSummarize}
              disabled={loading}
              className="bg-[#ff3838] hover:bg-[#e02e2e] text-white px-5 py-2 text-xs font-black uppercase tracking-wider flex items-center gap-1.5 cursor-pointer disabled:opacity-50"
            >
              {loading ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Sparkles className="w-3.5 h-3.5" />}
              <span>{loading ? 'Summarizing...' : 'Generate'}</span>
            </button>
          </div>
        </div>

        {/* Error Notice */}
        {error && (
          <div className="mb-4 p-3 bg-red-950/60 border border-red-800 text-red-300 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Summary Content Area */}
        <div className="flex-1 overflow-y-auto bg-[#0d0e12] border border-[#232630] p-4 text-sm font-sans text-gray-200 leading-relaxed space-y-2 select-text">
          {summary ? (
            <div className="whitespace-pre-wrap">{summary}</div>
          ) : (
            <div className="h-44 flex flex-col items-center justify-center text-gray-500 font-mono text-xs">
              <Sparkles className="w-8 h-8 mb-2 opacity-40" />
              <span>Click "Generate" to synthesize a bulleted summary of this material.</span>
            </div>
          )}
        </div>

        {/* Footer Actions */}
        {summary && (
          <div className="mt-4 pt-3 border-t border-[#262830] flex justify-end">
            <button
              onClick={copyToClipboard}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-[#171a24] border border-[#2b2f3d] hover:border-gray-400 text-xs font-mono text-gray-300 transition-colors"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-green-400" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copied ? 'Copied!' : 'Copy Summary'}</span>
            </button>
          </div>
        )}

      </div>
    </div>
  );
}
