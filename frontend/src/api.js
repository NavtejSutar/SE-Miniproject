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
    const errorMessage = (data && data.error) ? data.error : (typeof data === 'string' && data ? data : `Request failed with status ${response.status}`);
    throw new Error(errorMessage);
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
