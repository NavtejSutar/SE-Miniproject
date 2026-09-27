const API_BASE = '/api';

export const getAuthToken = () => localStorage.getItem('token');
export const setAuthToken = (token) => localStorage.setItem('token', token);
export const removeAuthToken = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
};

export const getStoredUser = () => {
  try {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  } catch {
    return null;
  }
};

export const setStoredUser = (user) => {
  localStorage.setItem('user', JSON.stringify(user));
};

async function request(endpoint, options = {}) {
  const token = getAuthToken();
  const headers = {
    ...(options.headers || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // If body is FormData, don't set Content-Type (browser will set multipart boundary)
  if (!(options.body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    removeAuthToken();
    window.dispatchEvent(new Event('auth:unauthorized'));
  }

  const contentType = response.headers.get('content-type') || '';
  let data = null;

  if (contentType.includes('application/json')) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    let rawError = (data && data.error) ? data.error : (typeof data === 'string' && data ? data : `Request failed with status ${response.status}`);
    if (typeof rawError === 'string' && (
      rawError.includes('could not execute statement') || 
      rawError.includes('relation') || 
      rawError.includes('column') || 
      rawError.includes('Hibernate') || 
      rawError.includes('SQL') || 
      rawError.includes('Exception')
    )) {
      rawError = 'A server error occurred. Please try again later.';
    }
    throw new Error(rawError);
  }

  return data;
}

export const api = {
  auth: {
    login: async (email, password) => {
      const data = await request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });
      if (data.token) {
        setAuthToken(data.token);
        setStoredUser({ email: data.email, username: data.username });
      }
      return data;
    },
    register: async (username, email, password) => {
      const data = await request('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, email, password }),
      });
      if (data.token) {
        setAuthToken(data.token);
        setStoredUser({ email: data.email, username: data.username });
      }
      return data;
    },
    logout: () => {
      removeAuthToken();
    },
  },

  documents: {
    upload: async (file) => {
      const formData = new FormData();
      formData.append('file', file);
      return request('/documents/upload', {
        method: 'POST',
        body: formData,
      });
    },
    list: async () => request('/documents'),
    get: async (id) => request(`/documents/${id}`),
    delete: async (id) => request(`/documents/${id}`, { method: 'DELETE' }),
    getProgress: async (id) => request(`/documents/${id}/progress`),
    getProgressStreamUrl: (id) => `${API_BASE}/documents/${id}/progress/stream`,
    summarize: async (id, pageNumbers = null) => {
      return request(`/documents/${id}/summarize`, {
        method: 'POST',
        body: JSON.stringify(pageNumbers ? { pageNumbers } : {}),
      });
    },
  },

  conversations: {
    list: async () => request('/conversations'),
    create: async (title = 'New Study Session') => {
      return request(`/conversations?title=${encodeURIComponent(title)}`, {
        method: 'POST',
      });
    },
    getMessages: async (id) => request(`/conversations/${id}/messages`),
    rename: async (id, title) => {
      return request(`/conversations/${id}?title=${encodeURIComponent(title)}`, {
        method: 'PUT',
      });
    },
    delete: async (id) => request(`/conversations/${id}`, { method: 'DELETE' }),
  },

  chat: {
    send: async (conversationId, prompt) => {
      return request('/chat', {
        method: 'POST',
        body: JSON.stringify({ conversationId, prompt }),
      });
    },
    stream: async (conversationId, prompt, { onToken, onSources, onDone, onError }) => {
      const token = getAuthToken();
      const headers = {
        'Content-Type': 'application/json',
      };
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      try {
        const response = await fetch(`${API_BASE}/chat/stream`, {
          method: 'POST',
          headers,
          body: JSON.stringify({ conversationId, prompt }),
        });

        if (!response.ok) {
          throw new Error(`Failed to stream chat: ${response.status}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        let currentEvent = 'message';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop();

          for (const line of lines) {
            if (line.startsWith('event:')) {
              currentEvent = line.substring(6).trim();
            } else if (line.startsWith('data:')) {
              const rawData = line.substring(5);
              if (currentEvent === 'sources') {
                try {
                  const sources = JSON.parse(rawData.trim());
                  if (onSources) onSources(sources);
                } catch (e) {}
              } else if (currentEvent === 'token') {
                try {
                  const parsed = JSON.parse(rawData.trim());
                  if (onToken && parsed && parsed.token !== undefined) {
                    onToken(parsed.token);
                  } else if (onToken) {
                    onToken(rawData);
                  }
                } catch (e) {
                  if (onToken) onToken(rawData);
                }
              } else if (currentEvent === 'error') {
                try {
                  const errObj = JSON.parse(rawData.trim());
                  if (onError) onError(new Error(errObj.error || 'Generation error'));
                } catch {
                  if (onError) onError(new Error(rawData.trim() || 'Generation error'));
                }
              } else if (currentEvent === 'done') {
                if (onDone) onDone();
              }
            }
          }
        }
        if (onDone) onDone();
      } catch (err) {
        if (onError) onError(err);
      }
    },
  },

  folders: {
    list: async () => request('/folders'),
    create: async (name) => {
      return request(`/folders?name=${encodeURIComponent(name)}`, {
        method: 'POST',
      });
    },
    rename: async (id, name) => {
      return request(`/folders/${id}?name=${encodeURIComponent(name)}`, {
        method: 'PUT',
      });
    },
    delete: async (id) => request(`/folders/${id}`, { method: 'DELETE' }),
    assign: async (folderId, docId) => {
      return request(`/folders/${folderId}/documents/${docId}`, {
        method: 'PUT',
      });
    },
    unassign: async (docId) => {
      return request(`/folders/documents/${docId}`, {
        method: 'DELETE',
      });
    },
  },

  search: {
    query: async (q) => request(`/search?q=${encodeURIComponent(q)}`),
  },
};
