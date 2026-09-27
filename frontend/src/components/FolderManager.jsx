import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { Folder, Plus, Trash2, Edit2, Check, X, FileText, AlertCircle, Loader2 } from 'lucide-react';

export default function FolderManager() {
  const [folders, setFolders] = useState([]);
  const [newFolderName, setNewFolderName] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editName, setEditName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadFolders = async () => {
    try {
      setLoading(true);
      const data = await api.folders.list();
      setFolders(data);
      setError('');
    } catch (err) {
      setError(err.message || 'Failed to load folders');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFolders();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!newFolderName.trim()) return;
    try {
      const created = await api.folders.create(newFolderName.trim());
      setFolders(prev => [...prev, created]);
      setNewFolderName('');
    } catch (err) {
      setError(err.message || 'Failed to create folder');
    }
  };

  const handleRename = async (id) => {
    if (!editName.trim()) return;
    try {
      const updated = await api.folders.rename(id, editName.trim());
      setFolders(prev => prev.map(f => f.folderId === id ? updated : f));
      setEditingId(null);
    } catch (err) {
      setError(err.message || 'Failed to rename folder');
    }
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete folder "${name}"? (Documents will be unassigned, not deleted)`)) return;
    try {
      await api.folders.delete(id);
      setFolders(prev => prev.filter(f => f.folderId !== id));
    } catch (err) {
      setError(err.message || 'Failed to delete folder');
    }
  };

  return (
    <div className="p-6 md:p-8 space-y-6 max-w-7xl mx-auto text-[#f5f6f8]">
      
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-[#26282f] pb-6">
        <div>
          <h2 className="text-2xl font-black uppercase tracking-wider text-white">Subject Folders</h2>
          <p className="text-xs text-gray-400 font-mono mt-1">Group your study materials by course or subject.</p>
        </div>

        {/* Create Folder Form */}
        <form onSubmit={handleCreate} className="flex gap-2">
          <input
            type="text"
            value={newFolderName}
            onChange={(e) => setNewFolderName(e.target.value)}
            placeholder="New Subject (e.g. Operating Systems)"
            className="bg-[#15171e] border border-[#2c2f3b] text-white px-3 py-2 text-xs font-mono focus:outline-none focus:border-[#ff3838] w-64"
          />
          <button
            type="submit"
            className="bg-[#ff3838] hover:bg-[#e02e2e] text-white px-4 py-2 text-xs font-black uppercase tracking-wider flex items-center gap-1.5 cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            <span>Create</span>
          </button>
        </form>
      </div>

      {error && (
        <div className="p-3 bg-red-950/60 border border-red-800 text-red-300 text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Folders Grid */}
      {loading ? (
        <div className="py-16 text-center text-xs font-mono text-gray-400 flex items-center justify-center gap-2">
          <Loader2 className="w-4 h-4 animate-spin text-[#ff3838]" /> Loading folders...
        </div>
      ) : folders.length === 0 ? (
        <div className="p-12 border border-[#242630] bg-[#0f1015] text-center font-mono text-xs text-gray-400">
          No folders created yet. Add a subject above to organize your notes.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {folders.map((fld) => (
            <div key={fld.folderId} className="border border-[#26282f] bg-[#111319] p-5 space-y-4">
              
              {/* Folder Title Row */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="p-2 bg-[#1a1d27] text-[#ff3838] border border-[#2d313e]">
                    <Folder className="w-4 h-4" />
                  </div>
                  {editingId === fld.folderId ? (
                    <div className="flex items-center gap-1">
                      <input
                        type="text"
                        value={editName}
                        onChange={(e) => setEditName(e.target.value)}
                        className="bg-[#1b1e28] text-white px-2 py-0.5 text-xs outline-none border border-gray-600"
                      />
                      <button onClick={() => handleRename(fld.folderId)} className="text-green-400 p-1">
                        <Check className="w-3.5 h-3.5" />
                      </button>
                      <button onClick={() => setEditingId(null)} className="text-gray-400 p-1">
                        <X className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  ) : (
                    <h3 className="font-bold text-sm text-white tracking-wide">{fld.name}</h3>
                  )}
                </div>

                <div className="flex items-center gap-1">
                  <button
                    onClick={() => { setEditingId(fld.folderId); setEditName(fld.name); }}
                    className="p-1 text-gray-400 hover:text-white"
                    title="Rename"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => handleDelete(fld.folderId, fld.name)}
                    className="p-1 text-gray-400 hover:text-red-400"
                    title="Delete"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              {/* Documents in this folder */}
              <div className="border-t border-[#1f222b] pt-3 space-y-2">
                <div className="text-[10px] font-mono text-gray-400 uppercase tracking-wider">
                  Documents ({fld.documents?.length || 0})
                </div>

                {fld.documents && fld.documents.length > 0 ? (
                  <div className="space-y-1.5 max-h-40 overflow-y-auto pr-1">
                    {fld.documents.map(doc => (
                      <div
                        key={doc.documentId}
                        className="flex items-center gap-2 p-2 bg-[#161821] border border-[#272b38] text-xs font-mono text-gray-300"
                      >
                        <FileText className="w-3.5 h-3.5 text-[#ff3838] flex-shrink-0" />
                        <span className="truncate flex-1">{doc.fileName}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-xs font-mono text-gray-600 py-2">
                    No documents assigned yet.
                  </div>
                )}
              </div>

            </div>
          ))}
        </div>
      )}

    </div>
  );
}
