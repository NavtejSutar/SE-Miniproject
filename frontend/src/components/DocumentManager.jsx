import React, { useState, useEffect, useRef } from 'react';
import { api } from '../api';
import { 
  Upload, FileText, Trash2, Folder, Sparkles, RefreshCw, 
  AlertCircle, CheckCircle2, Clock, Eye, X, Loader2
} from 'lucide-react';

export default function DocumentManager({ onOpenSummarize, onViewDocument }) {
  const [documents, setDocuments] = useState([]);
  const [folders, setFolders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(null); // { id, progress, message, fileName }
  const [error, setError] = useState('');
  const fileInputRef = useRef(null);
  const sseRef = useRef(null);

  const loadData = async () => {
    try {
      setLoading(true);
      const [docs, flds] = await Promise.all([
        api.documents.list(),
        api.folders.list()
      ]);
      setDocuments(docs);
      setFolders(flds);
      setError('');
    } catch (err) {
      setError(err.message || 'Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    return () => {
      if (sseRef.current) sseRef.current.close();
    };
  }, []);

  const handleFileUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validation
    const allowed = ['application/pdf', 'image/jpeg', 'image/png'];
    if (!allowed.includes(file.type)) {
      setError('Only PDF, JPEG, and PNG files are supported.');
      return;
    }

    if (file.size > 50 * 1024 * 1024) {
      setError('File size exceeds the 50 MB limit.');
      return;
    }

    try {
      setUploading(true);
      setError('');
      setUploadProgress({
        fileName: file.name,
        progress: 10,
        message: 'Uploading to server...'
      });

      const response = await api.documents.upload(file);
      
      // Document is saved, now listen to real-time progress via SSE or polling
      const docId = response.documentId;
      setUploadProgress({
        id: docId,
        fileName: response.fileName,
        progress: 20,
        message: 'Extracting text with PDFBox...'
      });

      // Poll progress every 1.5s until ready or failed
      const pollInterval = setInterval(async () => {
        try {
          const prog = await api.documents.getProgress(docId);
          setUploadProgress(prev => ({
            ...prev,
            progress: prog.progress,
            message: prog.progressMessage || 'Processing...'
          }));

          if (prog.status === 'READY') {
            clearInterval(pollInterval);
            setTimeout(() => {
              setUploadProgress(null);
              setUploading(false);
              loadData();
            }, 1000);
          } else if (prog.status === 'FAILED') {
            clearInterval(pollInterval);
            setError(prog.errorMessage || 'Processing failed');
            setUploading(false);
            setUploadProgress(null);
            loadData();
          }
        } catch (err) {
          clearInterval(pollInterval);
          setUploading(false);
          loadData();
        }
      }, 1500);

    } catch (err) {
      setError(err.message || 'Upload failed');
      setUploading(false);
      setUploadProgress(null);
    } finally {
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete document "${name}" and all its vector embeddings?`)) return;
    try {
      await api.documents.delete(id);
      setDocuments(prev => prev.filter(d => d.documentId !== id));
    } catch (err) {
      setError(err.message || 'Failed to delete document');
    }
  };

  const handleFolderAssign = async (docId, folderId) => {
    try {
      if (folderId) {
        await api.folders.assign(folderId, docId);
      } else {
        await api.folders.unassign(docId);
      }
      loadData();
    } catch (err) {
      setError(err.message || 'Failed to update folder assignment');
    }
  };

  return (
    <div className="p-6 md:p-8 space-y-6 max-w-7xl mx-auto text-[#f5f6f8]">
      
      {/* Header & Upload Trigger */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-[#26282f] pb-6">
        <div>
          <h2 className="text-2xl font-black uppercase tracking-wider text-white">Study Library & Documents</h2>
          <p className="text-xs text-gray-400 font-mono mt-1">Upload lecture notes, textbooks, and problem sets for RAG question answering.</p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={loadData}
            disabled={loading}
            className="p-2.5 bg-[#15171e] border border-[#2b2e38] text-gray-400 hover:text-white hover:border-gray-500 transition-colors"
            title="Refresh"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>

          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileUpload}
            accept=".pdf,.png,.jpg,.jpeg"
            className="hidden"
          />

          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={uploading}
            className="bg-[#ff3838] hover:bg-[#e02e2e] text-white px-5 py-2.5 text-xs font-black uppercase tracking-wider flex items-center gap-2 transition-transform active:scale-95 cursor-pointer disabled:opacity-50"
          >
            <Upload className="w-4 h-4" />
            <span>Upload Document</span>
          </button>
        </div>
      </div>

      {/* Error Notice */}
      {error && (
        <div className="p-4 bg-red-950/60 border border-red-800 text-red-300 text-sm flex items-center justify-between">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{error}</span>
          </div>
          <button onClick={() => setError('')}><X className="w-4 h-4" /></button>
        </div>
      )}

      {/* Real-Time Processing Progress Card */}
      {uploadProgress && (
        <div className="p-5 bg-[#14161f] border border-[#ff3838]/60 shadow-xl space-y-3 animate-fade-in">
          <div className="flex items-center justify-between text-xs font-mono">
            <div className="flex items-center gap-2 text-white font-bold">
              <Loader2 className="w-4 h-4 text-[#ff3838] animate-spin" />
              <span>{uploadProgress.fileName}</span>
            </div>
            <span className="text-[#ff3838] font-black">{Math.max(0, uploadProgress.progress)}%</span>
          </div>

          {/* Progress Bar */}
          <div className="w-full h-2 bg-[#20232d] overflow-hidden">
            <div
              className="h-full bg-[#ff3838] transition-all duration-300 ease-out"
              style={{ width: `${Math.max(5, uploadProgress.progress)}%` }}
            />
          </div>

          <div className="text-[11px] font-mono text-gray-400 flex items-center justify-between">
            <span>{uploadProgress.message}</span>
            <span>PgVector • Ollama</span>
          </div>
        </div>
      )}

      {/* Dropzone Area */}
      {documents.length === 0 && !loading && !uploading && (
        <div 
          onClick={() => fileInputRef.current?.click()}
          className="border-2 border-dashed border-[#292c36] hover:border-[#ff3838] bg-[#101217] p-12 text-center transition-colors cursor-pointer"
        >
          <div className="w-12 h-12 mx-auto mb-4 bg-[#1b1e27] border border-[#323642] flex items-center justify-center text-[#ff3838]">
            <Upload className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-white uppercase tracking-wider">No study documents uploaded yet</h3>
          <p className="text-xs text-gray-400 mt-1 font-mono">Click to upload lecture slides or PDFs (up to 50 MB)</p>
        </div>
      )}

      {/* Documents Table */}
      {documents.length > 0 && (
        <div className="border border-[#26282f] bg-[#0f1014] overflow-x-auto">
          <table className="w-full text-left text-xs font-mono">
            <thead className="bg-[#14161c] border-b border-[#26282f] text-gray-400 uppercase tracking-widest text-[10px]">
              <tr>
                <th className="py-3.5 px-4">Document</th>
                <th className="py-3.5 px-4">Size</th>
                <th className="py-3.5 px-4">Subject Folder</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4">Uploaded</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#21232a]">
              {documents.map((doc) => (
                <tr key={doc.documentId} className="hover:bg-[#14161d] transition-colors">
                  {/* File Name */}
                  <td className="py-4 px-4 font-sans font-bold text-sm text-white flex items-center gap-3">
                    <div className="p-2 bg-[#191b24] border border-[#2b2e3c] text-[#ff3838]">
                      <FileText className="w-4 h-4" />
                    </div>
                    <span className="truncate max-w-xs md:max-w-sm" title={doc.fileName}>{doc.fileName}</span>
                  </td>

                  {/* Size */}
                  <td className="py-4 px-4 text-gray-400">
                    {(doc.fileSize / (1024 * 1024)).toFixed(2)} MB
                  </td>

                  {/* Folder */}
                  <td className="py-4 px-4">
                    <select
                      value={doc.folderId || ''}
                      onChange={(e) => handleFolderAssign(doc.documentId, e.target.value ? Number(e.target.value) : null)}
                      className="bg-[#161820] border border-[#2b2e38] text-gray-300 py-1 px-2 text-xs focus:outline-none focus:border-[#ff3838]"
                    >
                      <option value="">General</option>
                      {folders.map(f => (
                        <option key={f.folderId} value={f.folderId}>{f.name}</option>
                      ))}
                    </select>
                  </td>

                  {/* Status Badge */}
                  <td className="py-4 px-4">
                    {doc.status === 'READY' && (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-green-950/40 border border-green-800 text-green-400 text-[10px] font-bold tracking-wider uppercase">
                        <CheckCircle2 className="w-3 h-3" /> Ready
                      </span>
                    )}
                    {doc.status === 'PROCESSING' && (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-amber-950/40 border border-amber-800 text-amber-400 text-[10px] font-bold tracking-wider uppercase">
                        <Loader2 className="w-3 h-3 animate-spin" /> {doc.progress || 0}%
                      </span>
                    )}
                    {doc.status === 'UPLOADED' && (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-blue-950/40 border border-blue-800 text-blue-400 text-[10px] font-bold tracking-wider uppercase">
                        <Clock className="w-3 h-3" /> Queued
                      </span>
                    )}
                    {doc.status === 'FAILED' && (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-red-950/40 border border-red-800 text-red-400 text-[10px] font-bold tracking-wider uppercase">
                        <AlertCircle className="w-3 h-3" /> Failed
                      </span>
                    )}
                  </td>

                  {/* Upload Date */}
                  <td className="py-4 px-4 text-gray-500 text-[11px]">
                    {doc.uploadedAt ? new Date(doc.uploadedAt).toLocaleDateString() : 'Just now'}
                  </td>

                  {/* Actions */}
                  <td className="py-4 px-4 text-right space-x-2">
                    <button
                      onClick={() => onOpenSummarize(doc)}
                      className="p-1.5 bg-[#171a22] border border-[#2c303d] text-gray-300 hover:text-white hover:border-[#ff3838] transition-colors"
                      title="Generate Summary"
                    >
                      <Sparkles className="w-3.5 h-3.5" />
                    </button>

                    <button
                      onClick={() => handleDelete(doc.documentId, doc.fileName)}
                      className="p-1.5 bg-[#171a22] border border-[#2c303d] text-red-400 hover:bg-red-950/50 hover:border-red-700 transition-colors"
                      title="Delete Document"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

    </div>
  );
}
