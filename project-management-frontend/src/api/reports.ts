import api from '.';

export const reportsApi = {
  getProjectStats: () => api.get('/reports/projects'),
  getTaskStats: (period: string) => api.get(`/reports/tasks?period=${period}`),
  getTeamPerformance: () => api.get('/reports/team'),
  exportReport: (type: string, format: string) => 
    api.get(`/reports/export/${type}?format=${format}`, {
      responseType: 'blob',
    }),
};