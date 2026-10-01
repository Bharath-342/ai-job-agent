import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('job_agent_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/register')) {
        localStorage.removeItem('job_agent_token');
        localStorage.removeItem('job_agent_user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  me: () => api.get('/auth/me'),
  getGoogleUrl: () => api.get('/auth/google/url'),
};

export const profileApi = {
  get: () => api.get('/profile'),
  update: (data) => api.put('/profile', data),
  uploadResume: (formData) => api.post('/profile/upload-resume', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
};

export const jobsApi = {
  getAll: () => api.get('/jobs'),
  getEligible: () => api.get('/jobs/eligible'),
  getMatch: (jobId) => api.get(`/jobs/${jobId}/match`),
  discover: () => api.post('/jobs/discover'),
};

export const applicationsApi = {
  getAll: () => api.get('/applications'),
  getById: (id) => api.get(`/applications/${id}`),
  apply: (data) => api.post('/applications/apply', data),
  resolveManual: (id, data) => api.post(`/applications/${id}/resolve-manual`, data),
};

export const emailApi = {
  getAll: () => api.get('/emails'),
  sync: () => api.post('/emails/sync'),
  simulate: (data) => api.post('/emails/simulate', data),
};

export const notificationsApi = {
  getAll: () => api.get('/notifications'),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markRead: (id) => api.put(`/notifications/${id}/read`),
  markAllRead: () => api.put('/notifications/read-all'),
};

export const settingsApi = {
  get: () => api.get('/settings'),
  update: (data) => api.put('/settings', data),
};

export const dashboardApi = {
  getStats: () => api.get('/dashboard'),
};

export const healthApi = {
  check: () => api.get('/health'),
};

export default api;
